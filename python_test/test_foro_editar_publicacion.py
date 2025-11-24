import subprocess
import time
import socket
import requests
import re
import pytest
import os

BASE = "http://localhost:8070"

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
    base = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    pom = os.path.join(base, "pom.xml")
    if os.path.exists(pom):
        return base
    return None


def test_editar_publicacion():
    proyecto_path = encontrar_ruta_proyecto()
    assert proyecto_path, "No se encontró pom.xml en la carpeta padre"

    proceso = subprocess.Popen(
        ["cmd", "/c", "mvn spring-boot:run"],
        cwd=proyecto_path,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE
    )

    try:
        assert esperar_servidor("localhost", 8070, 90), "Servidor no inició"

        session = requests.Session()

        login_page = session.get(BASE + "/login")
        csrf_login = obtener_csrf(login_page.text)
        assert csrf_login, "No se encontró CSRF en login"

        login_resp = session.post(
            BASE + "/login",
            data={
                "username": "admin@admin.com",
                "password": "admin123",
                "_csrf": csrf_login
            },
            allow_redirects=True
        )
        assert login_resp.status_code in (200, 302), "No se pudo iniciar sesión"

        # Cargar página para obtener CSRF
        foro_page = session.get(BASE + "/foro")
        assert foro_page.status_code == 200, "No se pudo cargar /foro"

        csrf_foro = obtener_csrf(foro_page.text)
        assert csrf_foro, "No se encontró CSRF en /foro"

        crear_resp = session.post(
            BASE + "/foro",
            data={
                "titulo": "Título Original",
                "contenido": "Contenido inicial de prueba",
                "_csrf": csrf_foro
            },
            allow_redirects=True
        )
        assert crear_resp.status_code in (200, 302), "No se pudo crear la publicación"

        foro_list = session.get(BASE + "/foro")
        assert foro_list.status_code == 200

        match = re.search(r'/foro/edit/(\d+)"', foro_list.text)
        assert match, "No se encontró ID de publicación"

        pub_id = match.group(1)

        edit_page = session.get(f"{BASE}/foro/edit/{pub_id}")
        assert edit_page.status_code == 200, "No se pudo cargar /foro/edit/{id}"

        csrf_edit = obtener_csrf(edit_page.text)
        assert csrf_edit, "No se encontró CSRF en el formulario de edición"

        update_resp = session.post(
            f"{BASE}/foro/update/{pub_id}",
            data={
                "titulo": "Título Actualizado",
                "contenido": "Contenido actualizado correctamente",
                "_csrf": csrf_edit
            },
            allow_redirects=True
        )

        assert update_resp.status_code in (200, 302), "Falló al actualizar publicación"

        final_page = session.get(BASE + "/foro")
        assert "Título Actualizado" in final_page.text, "No se reflejó la actualización"

    finally:
        proceso.terminate()
        try:
            proceso.wait(timeout=10)
        except subprocess.TimeoutExpired:
            proceso.kill()
