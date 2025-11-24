import requests
import re

BASE = "http://localhost:8070/calculadora"

def obtener_csrf(html):
    import re
    m = re.search(r'name="_csrf" value="([^"]+)"', html)
    return m.group(1) if m else None


def test_mostrar_resultado():
    session = requests.Session()

    # === 1) LOGIN REQUERIDO ===
    login_page = session.get("http://localhost:8070/login")
    csrf_login = obtener_csrf(login_page.text)
    assert csrf_login, "No se encontró CSRF en página de login"

    # === 2) Cargar pregunta 0 para obtener CSRF ===
    pagina = session.get("http://localhost:8070/calculadora?pregunta=0")
    assert pagina.status_code == 200, "No carga pregunta 0"

    csrf = obtener_csrf(pagina.text)
    assert csrf, "No se encontró CSRF en pregunta 0"

    # === 3) Enviar respuesta ===
    r1 = session.post(
        "http://localhost:8070/calculadora/responder",
        data={
            "respuesta": "25",
            "preguntaActual": "0",
            "_csrf": csrf
        },
        allow_redirects=True
    )

    assert r1.status_code in (200, 302), f"Respuesta devolvió {r1.status_code}"

    # === 4) Comprobar que redirige a la siguiente pregunta ===
    siguiente = session.get("http://localhost:8070/calculadora?pregunta=1")
    assert siguiente.status_code == 200
