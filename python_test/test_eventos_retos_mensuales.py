import subprocess
import time
import socket
import requests
import re
import pytest
import os

BASE_URL = "http://localhost:8070/eventos/retos/mensuales"

def esperar_servidor(host, puerto, timeout=90):
    inicio = time.time()
    while time.time() - inicio < timeout:
        try:
            with socket.create_connection((host, puerto), timeout=2):
                return True
        except OSError:
            time.sleep(1)
    return False

def obtener_csrf(html):
    match = re.search(r'name="_csrf" value="([^"]+)"', html)
    return match.group(1) if match else None

def encontrar_ruta_proyecto():
    rutas_posibles = [
        os.path.join(os.path.dirname(__file__), ".."),
        os.path.abspath(os.path.join(os.path.dirname(__file__), "..")),
    ]
    
    for ruta in rutas_posibles:
        pom = os.path.join(ruta, "pom.xml")
        if os.path.exists(pom):
            return ruta

    return None


def test_ver_retos_mensuales():
    proyecto_path = encontrar_ruta_proyecto()
    assert proyecto_path, "No se encontró pom.xml en la carpeta superior"

    # iniciar servidor
    proceso = subprocess.Popen(
        ["cmd", "/c", "mvn spring-boot:run"],
        cwd=proyecto_path,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE
    )

    try:
        # esperar servidor
        assert esperar_servidor("localhost", 8070, 90), "Servidor no inició"

        session = requests.Session()

        login_page = session.get("http://localhost:8070/login")
        csrf = obtener_csrf(login_page.text)
        assert csrf, "No se encontró token CSRF en /login"

        login_r = session.post(
            "http://localhost:8070/login",
            data={
                "username": "admin@admin.com",
                "password": "admin123",
                "_csrf": csrf
            },
            allow_redirects=True
        )

        assert login_r.status_code in (200, 302), f"Login falló: {login_r.status_code}"

        r = session.get(BASE_URL)
        assert r.status_code == 200, f"Status inesperado: {r.status_code}\nHTML:\n{r.text[:500]}"

        assert "retosPorMes" in r.text or "Reto" in r.text, "La vista no carga los retos"

    finally:
        proceso.terminate()
        try:
            proceso.wait(timeout=10)
        except subprocess.TimeoutExpired:
            proceso.kill()
