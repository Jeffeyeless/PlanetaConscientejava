import subprocess
import time
import socket
import requests
import re
import pytest
import os

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
    
    # Si no encuentra, mostrar error con las rutas verificadas
    print("❌ No se encontró el proyecto Spring Boot. Rutas verificadas:")
    for ruta in rutas_posibles:
        ruta_abs = os.path.abspath(ruta)
        existe = os.path.exists(ruta_abs)
        pom_existe = os.path.exists(os.path.join(ruta_abs, "pom.xml"))
        print(f"  {ruta_abs} - Existe: {existe} - Tiene pom.xml: {pom_existe}")
    
    return None

def test_calculadora_determinar_clasificacion():
    print("Buscando proyecto Spring Boot...")
    
    # Encontrar la ruta del proyecto
    proyecto_path = encontrar_ruta_proyecto()
    if not proyecto_path:
        pytest.fail("No se pudo encontrar el proyecto Spring Boot")
    
    print(f"Iniciando servidor desde: {proyecto_path}")

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
            pytest.fail("Servidor no inició en el tiempo esperado")

        session = requests.Session()

        # ===== LOGIN =====
        print("Realizando login...")
        login_page = session.get("http://localhost:8070/login")
        csrf_login = obtener_csrf(login_page.text)
        assert csrf_login, "No se encontró CSRF login"

        login_response = session.post(
            "http://localhost:8070/login",
            data={
                "username": "admin@admin.com",
                "password": "admin123",
                "_csrf": csrf_login
            }
        )

        print("✔ Login OK")

        # ===== RESPUESTAS CALCULADORA =====
        respuestas = [
            "18", "M", "0", "3", "0", "0", "0", "0", "0",
            "50", "1", "1", "100", "50", "3"
        ]

        for idx, respuesta in enumerate(respuestas):
            print(f"Respondiendo pregunta {idx + 1}/15: {respuesta}")

            # Obtener CSRF desde la página actual de la calculadora
            pagina = session.get(f"http://localhost:8070/calculadora?pregunta={idx}")
            assert pagina.status_code == 200, f"No carga pregunta {idx}"

            csrf = obtener_csrf(pagina.text)
            assert csrf, f"No se encontró CSRF en pregunta {idx}"

            r = session.post(
                "http://localhost:8070/calculadora/responder",
                data={
                    "respuesta": respuesta,
                    "preguntaActual": idx,
                    "_csrf": csrf
                }
            )

            assert r.status_code in (200, 302), f"Error enviando respuesta {idx}"

        # ===== VERIFICAR RESULTADO =====
        resultado = session.get("http://localhost:8070/calculadora/resultado")
        assert resultado.status_code == 200, "No se pudo obtener resultado"

        print("✔ Test completado correctamente")

    except Exception as e:
        print(f"❌ Error durante el test: {e}")
        raise
    finally:
        # Siempre terminar el proceso
        if 'proceso' in locals():
            print("Cerrando servidor...")
            proceso.terminate()
            try:
                proceso.wait(timeout=10)
            except subprocess.TimeoutExpired:
                proceso.kill()