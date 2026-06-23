# TymczasowyMail.pl - System Tymczasowej Poczty E-mail

W pełni funkcjonalny, lekki i bezpieczny system tymczasowej poczty e-mail (Temp Mail). Projekt składa się z backendu w języku **Java (Spring Boot)**, skryptu nasłuchującego pocztę SMTP w Pythonie oraz frontendu HTML/CSS/JS. DOSTEPNA POD ADRESEM : TymczasowyMail.pl

---

## 📋 Spis treści

- [Funkcjonalności](#-funkcjonalności)
- [Architektura Systemu](#-architektura-systemu)
- [Wymagania i Instalacja](#-wymagania-i-instalacja)
- [Konfiguracja i Uruchomienie](#-konfiguracja-i-uruchomienie)
- [Bezpieczeństwo i RODO](#-bezpieczeństwo-i-rodo)
- [Troubleshooting](#-troubleshooting)

---

## ✨ Funkcjonalności

- ✅ **Natychmiastowe tworzenie skrzynek** - Generuj tymczasowe adresy e-mail bez rejestracji
- ✅ **Automatyczne pobieranie maili** - Real-time polling co 5 sekund
- ✅ **Szyfrowanie STARTTLS** - Let's Encrypt SSL/TLS na porcie SMTP
- ✅ **Rate Limiting** - Ochrona przed spamem i atakami brute-force
- ✅ **Samoistne usuwanie** - Wiadomości i skrzynki usuwane po 24 godzinach
- ✅ **RODO Compliant** - Bez śledzenia, bez cookies, pełna anonimowość
- ✅ **Responsywny interfejs** - Działa na urządzeniach mobilnych i desktopowych

---

## 🏗️ Architektura Systemu

System opiera się na trzech głównych komponentach:

### 1. **Frontend (HTML/CSS/JS)**
- Jednostronicowa aplikacja (SPA)
- Czysty JavaScript bez frameworków
- Automatyczne sprawdzanie poczty (polling co 5 sekund)
- Responsywny design
- Google Fonts integracja

### 2. **Backend (Java Spring Boot)**
- Serwowanie frontendu
- Zarządzanie tokenami skrzynek
- REST API do pobierania maili
- Webhook `/webhooks/new` dla odbiornika SMTP
- Integracja z bazą danych MySQL

### 3. **Odbiornik Poczty (Python SMTP)**
- Serwer SMTP na porcie 25
- Obsługa STARTTLS (Let's Encrypt)
- Parsowanie wiadomości e-mail
- Rate limiting połączeń
- Integracja z backend poprzez webhook

```
┌─────────────┐
│   Browser   │
└──────┬──────┘
       │ HTTP/HTTPS
       ▼
┌─────────────────────┐
│   Nginx Reverse     │
│   Proxy + Rate      │
│   Limiting          │
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐         ┌──────────────┐
│  Java Spring Boot   │◄────────┤   MySQL DB   │
│  Backend (8080)     │         └──────────────┘
└──────┬──────────────┘
       │
       ▲ webhook
       │
       └──────────────────────┐
                              │
                         ┌────▼──────────────┐
                         │  Python SMTP      │
                         │  Receiver (25)    │
                         │  + Rate Limiter   │
                         └───────────────────┘
                              ▲
                              │ SMTP
                    ┌─────────┴──────────┐
                    │                    │
            ┌───────▼────────┐  ┌────────▼──────┐
            │  External Mail │  │   Spammers    │
            │  Servers       │  │  (Blocked)    │
            └────────────────┘  └───────────────┘
```

---

## 🛠️ Wymagania i Instalacja

### Wymagania systemowe
- **Java 21** lub nowsza
- **Maven 3.8+**
- **Python 3.10+**
- **Nginx** (serwer produkcyjny)
- **MySQL 8.0+** lub **MariaDB** (np. darmowy pakiet na Aiven)
- **Let's Encrypt certyfikaty** (darmowe certyfikaty SSL)
- **Linux VPS** (Ubuntu 20.04+ lub podobne)

### Przygotowanie Bazy Danych

1. Zarejestruj się na platformie [Aiven](https://aiven.io) (darmowy plan)
2. Utwórz bazę MySQL
3. Pobierz dane dostępowe (host, port, nazwa bazy, użytkownik, hasło)

### Zdobycie certyfikatów Let's Encrypt

```bash
sudo apt-get update
sudo apt-get install certbot
sudo certbot certonly --standalone -d tymczasowymail.pl -d www.tymczasowymail.pl
```

---

## ⚙️ Konfiguracja i Uruchomienie

### Krok 1: Konfiguracja Spring Boot (Backend)

1. Przejdź do katalogu projektu:
```bash
cd /path/to/project
```

2. Edytuj plik `src/main/resources/application.properties`:
```properties
# Konfiguracja bazy danych
spring.datasource.url=jdbc:mysql://<AIVEN_HOST>:<AIVEN_PORT>/<DATABASE_NAME>?sslMode=REQUIRED&serverTimezone=UTC
spring.datasource.username=<TWÓJ_UŻYTKOWNIK>
spring.datasource.password=<TWOJE_HASŁO>

# Port aplikacji
server.port=8080

# Timeout
server.servlet.session.timeout=30m
```

3. Kompilacja i budowanie:
```bash
./mvnw clean package -DskipTests
```

4. Uruchomienie aplikacji:
```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

**Wynik:**
```
2026-06-23 10:15:32 ... : Started Application in 8.234 seconds
```

---

### Krok 2: Konfiguracja Odbiornika SMTP (Python)

1. Przejdź do głównego katalogu:
```bash
cd /path/to/project
```

2. Stwórz środowisko wirtualne:
```bash
python3 -m venv venv
source venv/bin/activate
```

3. Zainstaluj zależności:
```bash
pip install aiosmtpd
```

4. Edytuj plik `receiver.py` - ustaw ścieżki do certyfikatów:
```python
CERT_FILE = '/etc/letsencrypt/live/tymczasowymail.pl/fullchain.pem'
KEY_FILE = '/etc/letsencrypt/live/tymczasowymail.pl/privkey.pem'
```

5. Uruchomienie serwera (wymaga uprawnień sudo):
```bash
sudo ./venv/bin/python receiver.py
```

**Wynik:**
```
[SMTP] Certyfikaty SSL załadowane pomyślnie. Szyfrowanie TLS włączone.
SMTP Serwer działa na 0.0.0.0:25 (Single Thread)...
```

### Systemd Service (Opcjonalnie)

Aby receiver.py uruchamiał się automatycznie, stwórz usługę systemd:

```bash
sudo nano /etc/systemd/system/smtp-receiver.service
```

Zawartość:
```ini
[Unit]
Description=TymczasowyMail SMTP Receiver
After=network.target

[Service]
Type=simple
User=www-data
WorkingDirectory=/path/to/project
ExecStart=/path/to/project/venv/bin/python receiver.py
Restart=on-failure
RestartSec=10s

[Install]
WantedBy=multi-user.target
```

Włącz usługę:
```bash
sudo systemctl enable smtp-receiver
sudo systemctl start smtp-receiver
```

---

### Krok 3: Konfiguracja Nginx (Reverse Proxy + Rate Limiting)

1. Edytuj lub stwórz plik `/etc/nginx/sites-available/tymczasowymail.pl`:

```nginx
# Limitowanie zapytań HTTP na sekundę z jednego IP
limit_req_zone $binary_remote_addr zone=limit_ip:10m rate=5r/s;

server {
    listen 80;
    server_name tymczasowymail.pl www.tymczasowymail.pl;

    # Redirect HTTP do HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name tymczasowymail.pl www.tymczasowymail.pl;

    # Certyfikaty SSL
    ssl_certificate /etc/letsencrypt/live/tymczasowymail.pl/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/tymczasowymail.pl/privkey.pem;

    # Konfiguracja SSL
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    # Rate limiting
    limit_req zone=limit_ip burst=10 nodelay;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
}
```

2. Aktywuj konfigurację:
```bash
sudo ln -s /etc/nginx/sites-available/tymczasowymail.pl /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

---

## 🔒 Bezpieczeństwo i RODO

### Bezpieczeństwo sieci
- ✅ Backend Java nasłuchuje wyłącznie na `127.0.0.1:8080` (brak dostępu z zewnątrz bez proxy)
- ✅ Reverse proxy Nginx filtruje i rate limituje zapytania
- ✅ HTTPS obowiązkowy - Let's Encrypt SSL/TLS
- ✅ STARTTLS na porcie SMTP 25

### RODO Compliance
- ✅ Brak analityki (Google Analytics, Matomo itp.)
- ✅ Brak plików śledzących (cookies)
- ✅ Brak zbierania danych osobowych
- ✅ Dostępne regulamin i polityka prywatności

### Ochrona przed spamem
- ✅ **Rate limiting Python:** 30 połączeń na 60 sekund z jednego IP
- ✅ **Rate limiting Nginx:** 5 żądań na sekundę z jednego IP
- ✅ Automatyczne zablokowanie połączeń przekraczających limit
- ✅ Czyszczenie logów połączeń co 60 sekund

### Automatyczne usuwanie
- ✅ Wszystkie wiadomości usuwane po 24 godzinach
- ✅ Skrzynki tymczasowe usuwane po 24 godzinach
- ✅ Bezpowrotne usunięcie danych

---

## 📊 Monitorowanie

### Logi aplikacji Java
```bash
tail -f nohup.out
```

### Logi SMTP Receiver
```bash
sudo journalctl -u smtp-receiver -f
```

### Logi Nginx
```bash
sudo tail -f /var/log/nginx/error.log
sudo tail -f /var/log/nginx/access.log
```

### Statystyka bazy danych
```sql
SELECT COUNT(*) as liczba_skrzynek FROM mailboxes;
SELECT COUNT(*) as liczba_wiadomosci FROM emails;
```

---

## 🐛 Troubleshooting

### Problem: "Blad przekazywania" w logach SMTP

**Rozwiązanie:**
1. Sprawdź czy backend Java działa: `curl http://127.0.0.1:8080`
2. Sprawdź porty: `netstat -tulpn | grep 8080`
3. Sprawdź łączność: `curl -X POST http://127.0.0.1:8080/webhooks/new`

### Problem: Port 25 nie otwiera się

**Rozwiązanie:**
```bash
# Sprawdź czy proces działa
sudo ps aux | grep receiver.py

# Sprawdź uprawnienia
sudo lsof -i :25

# Restartuj receiver
sudo systemctl restart smtp-receiver
```

### Problem: Certyfikaty SSL nie ładują się

**Rozwiązanie:**
```bash
# Sprawdź ścieżki
ls -la /etc/letsencrypt/live/tymczasowymail.pl/

# Uprawnienia
sudo chmod -R 644 /etc/letsencrypt/live/
sudo chmod 755 /etc/letsencrypt/live/

# Odnów certyfikat
sudo certbot renew --force-renewal
```

### Problem: Rate limiting blokuje legalne zapytania

**Rozwiązanie:**
Zwiększ limit w `receiver.py`:
```python
LIMIT_POLACZEN = 50      # Zwiększ z 30 na 50
OKNO_CZASOWE = 120       # Zwiększ z 60 na 120 sekund
```

Lub w `nginx.conf`:
```nginx
rate=10r/s;             # Zwiększ z 5r/s na 10r/s
```

---

## 📈 Statystyki projektu

- **Języki:** HTML (73.1%), Java (14.8%), Python (9.2%), CSS (2.9%)
- **Architektura:** Microservices + Reverse Proxy
- **Baza danych:** MySQL (chmura Aiven)
- **Certyfikaty:** Let's Encrypt (auto-renew)
- **Uptime:** 24/7 (z automatycznym restartowaniem)

---

**Ostatnia aktualizacja:** 2026-06-23 | **Wersja:** 1.0.0
