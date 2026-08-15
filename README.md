# TymczasowyMail.pl - System Tymczasowej Poczty E-mail

W pełni funkcjonalny, lekki i bezpieczny system tymczasowej poczty e-mail (Temp Mail). Projekt składa się z backendu w języku **Java (Spring Boot)**, skryptu nasłuchującego pocztę SMTP w Pythonie oraz frontendu HTML/CSS/JS

---

## 📋 Spis treści

- [Funkcjonalności](#-funkcjonalności)
- [Architektura Systemu](#-architektura-systemu)
- [Wymagania](#-wymagania)


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

## 🛠️ Wymagania

### Wymagania systemowe
- **Java 21** lub nowsza
- **Maven 3.8+**
- **Python 3.10+**
- **Nginx** (serwer produkcyjny)
- **MySQL 8.0+** lub **MariaDB** (np. darmowy pakiet na Aiven)
- **Let's Encrypt certyfikaty** (darmowe certyfikaty SSL)
- **Linux VPS** (Ubuntu 20.04+ lub podobne)


## 📈 Statystyki projektu

- **Języki:** HTML (73.1%), Java (14.8%), Python (9.2%), CSS (2.9%)
- **Architektura:** Microservices + Reverse Proxy
- **Baza danych:** MySQL (chmura Aiven)
- **Certyfikaty:** Let's Encrypt (auto-renew)
- **Uptime:** 24/7 (z automatycznym restartowaniem)

---

**Ostatnia aktualizacja:** 2026-06-23 | **Wersja:** 1.0.0
