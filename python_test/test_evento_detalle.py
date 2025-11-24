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

def test_ver_detalle_evento():
    print("🚀 Buscando proyecto Spring Boot...")
    
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
        assert csrf_login, "No se encontró CSRF en login"

        login_response = session.post(
            "http://localhost:8070/login",
            data={
                "username": "admin@admin.com",
                "password": "admin123",
                "_csrf": csrf_login
            }
        )
        assert login_response.status_code in (200, 302), "❌ Login falló"
        print("✔ Login correcto")

        # ========== CREAR EVENTO DE PRUEBA ==========
        print("📝 Creando evento de prueba...")
        page_new = session.get("http://localhost:8070/eventos/nuevo")
        csrf_new = obtener_csrf(page_new.text)
        assert csrf_new, "No se encontró CSRF en formulario de nuevo evento"

        datos_evento = {
            "titulo": "Evento Test Detalle",
            "descripcion": "Descripción de prueba para ver detalle",
            "ubicacion": "Bogotá",
            "fechaHora": "2025-12-31T18:00",
            "_csrf": csrf_new
        }

        create_response = session.post(
            "http://localhost:8070/eventos/guardar", 
            data=datos_evento, 
            allow_redirects=False
        )
        assert create_response.status_code == 302, "❌ Error al crear el evento"

        # Obtener ID del último evento creado
        eventos_page = session.get("http://localhost:8070/eventos")
        assert eventos_page.status_code == 200, "❌ No se pudo cargar la lista de eventos"
        
        ids = re.findall(r"/eventos/(\d+)", eventos_page.text)
        assert ids, "❌ No se encontraron eventos en la lista"

        id_evento = max([int(x) for x in ids])  # tomar el último
        print(f"✔ Evento creado con ID = {id_evento}")

        # ========== VER DETALLE ==========
        print(f"🔍 Verificando detalle del evento {id_evento}...")
        detail_response = session.get(f"http://localhost:8070/eventos/{id_evento}")
        assert detail_response.status_code == 200, f"❌ No se pudo acceder al detalle del evento {id_evento}"

        assert "Evento Test Detalle" in detail_response.text, "❌ El detalle del evento no contiene el título esperado"
        assert "Bogotá" in detail_response.text, "❌ El detalle del evento no contiene la ubicación esperada"

        print(f"✔ Detalle del evento {id_evento} cargado correctamente")
        print("✅ Test completado exitosamente")

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