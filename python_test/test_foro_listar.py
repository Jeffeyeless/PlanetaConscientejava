import subprocess
import time
import socket
import re
import requests
import os
import pytest

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
        ruta_abs = os.path.abspath(ruta)
        pom_path = os.path.join(ruta_abs, "pom.xml")
        if os.path.exists(pom_path):
            print(f"✓ Proyecto encontrado en: {ruta_abs}")
            return ruta_abs
    
    print("❌ No se encontró el proyecto Spring Boot. Rutas verificadas:")
    for ruta in rutas_posibles:
        ruta_abs = os.path.abspath(ruta)
        existe = os.path.exists(ruta_abs)
        pom_existe = os.path.exists(os.path.join(ruta_abs, "pom.xml"))
        print(f"  {ruta_abs} - Existe: {existe} - Tiene pom.xml: {pom_existe}")
    
    return None

def test_listar_foro():
    print("\n🚀 Buscando proyecto Spring Boot...")
    
    # Encontrar la ruta del proyecto
    proyecto_path = encontrar_ruta_proyecto()
    if not proyecto_path:
        pytest.fail("No se pudo encontrar el proyecto Spring Boot")
    
    print(f"📁 Iniciando servidor desde: {proyecto_path}")

    try:
        proceso = subprocess.Popen(
            ["cmd", "/c", "mvn spring-boot:run"],
            cwd=proyecto_path,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE
        )

        # Esperar a que el servidor inicie
        if not esperar_servidor("localhost", 8070, 90):
            proceso.terminate()
            pytest.fail("❌ El servidor no inició en el tiempo esperado")

        session = requests.Session()

        # ========== LOGIN ==========
        print("🔐 Realizando login...")
        login_page = session.get("http://localhost:8070/login")
        csrf_login = obtener_csrf(login_page.text)
        assert csrf_login, "❌ No se encontró CSRF en login"

        login_response = session.post(
            "http://localhost:8070/login",
            data={
                "username": "admin@admin.com",
                "password": "admin123",
                "_csrf": csrf_login
            },
            allow_redirects=False
        )

        assert login_response.status_code in (200, 302), "❌ Error al iniciar sesión"
        print("✔ Inicio de sesión correcto")

        # ========== CREAR PUBLICACIÓN EN FORO ==========
        print("📝 Creando publicación en el foro...")
        foro_page = session.get("http://localhost:8070/foro")
        assert foro_page.status_code == 200, "❌ No se pudo acceder al foro"
        
        csrf_foro = obtener_csrf(foro_page.text)
        assert csrf_foro, "❌ No se encontró el CSRF en el formulario del foro"

        titulo_test = "Título de Prueba Foro - Test Automático"
        contenido_test = "Contenido generado desde el test de listar foro"

        create_response = session.post(
            "http://localhost:8070/foro",
            data={
                "titulo": titulo_test,
                "contenido": contenido_test,
                "_csrf": csrf_foro
            },
            allow_redirects=False
        )

        assert create_response.status_code in (200, 302), "❌ Error al crear publicación del foro"
        print("✔ Publicación creada correctamente")

        # ========== VERIFICAR QUE APARECE EN LA LISTA ==========
        print("🔍 Verificando publicación en la lista del foro...")
        list_response = session.get("http://localhost:8070/foro")
        assert list_response.status_code == 200, "❌ No se pudo acceder a /foro"

        # Verificar que la publicación aparece en la lista
        assert titulo_test in list_response.text, "❌ El título no aparece en la lista del foro"
        assert contenido_test in list_response.text, "❌ El contenido no aparece en la lista del foro"

        print("✔ La publicación aparece correctamente en la lista del foro")
        print("✅ Test de listar foro completado exitosamente")

    except Exception as e:
        print(f"❌ Error durante el test: {e}")
        raise
    finally:
        # Siempre terminar el proceso
        if 'proceso' in locals():
            print("🛑 Cerrando servidor...")
            proceso.terminate()
            try:
                proceso.wait(timeout=10)
            except subprocess.TimeoutExpired:
                proceso.kill()