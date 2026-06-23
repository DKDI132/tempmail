import asyncio
import json
import urllib.request
import urllib.error
import ssl  
import time
import datetime
import logging
from collections import defaultdict
from aiosmtpd.controller import Controller
from aiosmtpd.smtp import SMTP
from email import message_from_bytes

# Wyciszenie logowania ostrzeżeń aiosmtpd (np. komend GET/POST wysyłanych przez boty)
logging.getLogger('mail.log').setLevel(logging.ERROR)

CERT_FILE = '/etc/letsencrypt/live/tymczasowymail.pl/fullchain.pem'
KEY_FILE = '/etc/letsencrypt/live/tymczasowymail.pl/privkey.pem'

def pobierz_kontekst_ssl():
    try:
        context = ssl.create_default_context(ssl.Purpose.CLIENT_AUTH)
        context.load_cert_chain(certfile=CERT_FILE, keyfile=KEY_FILE)
        print("[SMTP] Certyfikaty SSL załadowane pomyślnie. Szyfrowanie TLS włączone.")
        return context
    except Exception as e:
        print(f"[SMTP WARNING] Nie można załadować certyfikatów SSL ({e}). Serwer uruchomi się bez szyfrowania.")
        return None

def parsuj_maila(surowe_bajty):
    msg = message_from_bytes(surowe_bajty)
    subject = msg['Subject'] or "(Bez tematu)"
    
    body = ""
    if msg.is_multipart():
        for czesc in msg.walk():
            typ = czesc.get_content_type()
            if typ == "text/plain":
                body = czesc.get_payload(decode=True).decode('utf-8', errors='ignore')
                break
    else:
        body = msg.get_payload(decode=True).decode('utf-8', errors='ignore')
        
    return subject, body

class PocztaHandler:
    async def handle_DATA(self, server, session, envelope):
        sender = envelope.mail_from
        recipient = envelope.rcpt_tos[0] if envelope.rcpt_tos else ""
        
  
        subject, body = parsuj_maila(envelope.content)
   
        if recipient.lower().startswith('support@'):
            try:
                zapis = (
                    f"==================================================\n"
                    f"DATA: {datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n"
                    f"OD: {sender}\n"
                    f"DO: {recipient}\n"
                    f"TEMAT: {subject}\n"
                    f"TREŚĆ:\n{body}\n"
                    f"==================================================\n\n"
                )
                with open("maile.txt", "a", encoding="utf-8") as f:
                    f.write(zapis)
                print(f"[SUPPORT] Zapisano maila do supportu od {sender} do pliku maile.txt")
            except Exception as e:
                print(f"[SUPPORT ERROR] Nie udało się zapisać maila do pliku: {e}")
            return '250 Message accepted for delivery'
        

        payload = {
            "sender": sender,
            "recipient": recipient,
            "subject": subject,
            "body": body
        }
        
        dane_json = json.dumps(payload).encode('utf-8')
        req = urllib.request.Request('http://127.0.0.1:8080/webhooks/new', data=dane_json, method='POST')
        req.add_header('Content-Type', 'application/json')
        
        try:
            with urllib.request.urlopen(req) as response:
                if response.status == 200:
                    print(f"[SMTP] Przekazano maila! Od: {sender}, Temat: {subject}")
                    return '250 Message accepted for delivery'
        except urllib.error.HTTPError as e:
            if e.code == 400:
                try:
                    res_body = e.read().decode('utf-8')
                    info = json.loads(res_body)
                    if "nie istnieje" in info.get("message", "").lower():
                        print(f"[SMTP] Odrzucono (odbiorca nie istnieje): {recipient}")
                        return '550 No such user here'
                except Exception:
                    pass
            print(f"[SMTP] Blad przekazywania HTTP {e.code}: {e.reason}")
        except Exception as e:
            print(f"[SMTP] Blad przekazywania: {e}")
            
        return '451 Local error in processing'

POLACZENIA_IP = defaultdict(list)
LIMIT_POLACZEN = 30  # Maksymalnie 30 połączeń
OKNO_CZASOWE = 60    # w ciągu 60 sekund (1 minuta)

def czy_limit_przekroczony(ip):
    if len(POLACZENIA_IP[ip]) >= LIMIT_POLACZEN:
        return True
    
    POLACZENIA_IP[ip].append(time.time())
    return False

async def czysciciel_limitow():
    while True:
        await asyncio.sleep(60)
        teraz = time.time()
        for ip in list(POLACZENIA_IP.keys()):
            POLACZENIA_IP[ip] = [t for t in POLACZENIA_IP[ip] if teraz - t < OKNO_CZASOWE]
            if not POLACZENIA_IP[ip]:
                del POLACZENIA_IP[ip]

class CustomSMTP(SMTP):
    def connection_made(self, transport):
        peer = transport.get_extra_info('peername')
        print(f"[SMTP] Nowe połączenie TCP z: {peer}")
        super().connection_made(transport)
        if peer and isinstance(peer, (tuple, list)):
            ip = peer[0]
            if czy_limit_przekroczony(ip):
                print(f"[SMTP WARNING] Zablokowano połączenie (rate limit) z IP: {ip}")
                transport.close()
                return

if __name__ == '__main__':
    handler = PocztaHandler()
    ssl_ctx = pobierz_kontekst_ssl()
    
    loop = asyncio.get_event_loop()
    

    server_coro = loop.create_server(
        lambda: CustomSMTP(handler, tls_context=ssl_ctx),
        host='0.0.0.0',
        port=25
    )
    server = loop.run_until_complete(server_coro)
    
    # Rejestrujemy czyszczenie limitów w tle
    loop.create_task(czysciciel_limitow())
    
    print("SMTP Serwer działa na 0.0.0.0:25 (Single Thread)...")
    
    try:
        loop.run_forever()
    except KeyboardInterrupt:
        print("Zamykanie serwera SMTP...")
        server.close()
        loop.run_until_complete(server.wait_closed())