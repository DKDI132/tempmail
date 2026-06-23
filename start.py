import os
import json
import subprocess
import sys

def uruchom_backend():
    sciezka_config = "config.json"
    
    # 1. Wczytujemy zmienne z pliku konfiguracyjnego (jeśli istnieje)
    if not os.path.exists(sciezka_config):
        print(f"[INFO] Brak pliku {sciezka_config} - uruchamiam z domyślnymi zmiennymi środowiskowymi (localhost).")
        env_vars = os.environ.copy()
    else:
        with open(sciezka_config, "r", encoding="utf-8") as f:
            dane_bazy = json.load(f)
        
        env_vars = os.environ.copy()
        env_vars["DB_URL"] = dane_bazy.get("DB_URL", "")
        env_vars["DB_USER"] = dane_bazy.get("DB_USER", "")
        env_vars["DB_PASS"] = dane_bazy.get("DB_PASS", "")
        print(f"[OK] Wczytano dane dostępowe bazy z pliku {sciezka_config}.")

    # 2. Szukamy zbudowanego pliku .jar
    sciezka_jar = "target/demo-0.0.1-SNAPSHOT.jar"
    if not os.path.exists(sciezka_jar):
        print(f"[BŁĄD] Nie znaleziono pliku {sciezka_jar}!")
        print("Najpierw zbuduj aplikację poleceniem: ./mvnw clean package -DskipTests")
        sys.exit(1)

    # 3. Uruchamiamy proces Javy z wstrzykniętymi zmiennymi środowiskowymi
    folder_jdk = "jdk-21.0.2+13"  # Możesz tu podać ścieżkę względną lub bezwzględną (np. "C:/Java/jdk...")
    # 2. Ustalamy nazwę pliku w zależności od systemu (Windows ma rozszerzenie .exe)
    binarka_java = "java.exe" if os.name == "nt" else "java"
    # 3. Łączymy ścieżki w jedną: np. "jdk-21.0.2+13/bin/java.exe"
    sciezka_java = os.path.join(folder_jdk, "bin", binarka_java)
    # Sprawdzamy czy ta ręczna Java w ogóle istnieje w podanej ścieżce
    if not os.path.exists(sciezka_java):
        print(f"[BŁĄD] Nie znaleziono Javy w ścieżce: {sciezka_java}!")
        sys.exit(1)
    # 4. Uruchamiamy proces używając naszej ręcznej ścieżki
    print(f"[START] Uruchamianie serwera za pomocą JDK z: {folder_jdk}...")
    try:
        subprocess.run([sciezka_java, "-jar", sciezka_jar], env=env_vars)
    except KeyboardInterrupt:
        print("\n[STOP] Zatrzymano serwer backendu.")

if __name__ == "__main__":
    uruchom_backend()