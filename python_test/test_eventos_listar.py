import requests
from bs4 import BeautifulSoup
import re

BASE_URL = "http://localhost:8070"

def extraer_csrf(html):
    """Extrae el token CSRF de cualquier formulario."""
    match = re.search(r'name="_csrf"\s+value="([^"]+)"', html)
    assert match, "No se encontró el CSRF en el formulario"
    return match.group(1)

def test_eventos_listar_eventos_y_retos():
    session = requests.Session()

    print("➡️ Iniciando sesión...")

    # 1. Obtener login page
    login_page = session.get(f"{BASE_URL}/login")
    assert login_page.status_code == 200, "No se pudo cargar /login"

    csrf_login = extraer_csrf(login_page.text)

    # 2. Enviar credenciales
    login_resp = session.post(
        f"{BASE_URL}/login",
        data={
            "username": "admin@admin.com",
            "password": "admin123",
            "_csrf": csrf_login
        },
        allow_redirects=True
    )

    assert login_resp.status_code == 200, "❌ No se pudo iniciar sesión"

    print("✅ Sesión iniciada correctamente")

    # 3. Abrir formulario de nuevo evento
    print("➡️ Cargando /eventos/nuevo ...")
    form_page = session.get(f"{BASE_URL}/eventos/nuevo")
    assert form_page.status_code == 200, "❌ No se pudo cargar /eventos/nuevo"

    csrf_form = extraer_csrf(form_page.text)

    print("➡️ Creando evento...")

    # 4. Enviar formulario
    form_data = {
        "titulo": "Evento de Prueba",
        "descripcion": "Descripción generada automáticamente",
        "fechaHora": "2025-01-01T10:00",
        "ubicacion": "Colombia",
        "_csrf": csrf_form
    }

    submit_resp = session.post(
        f"{BASE_URL}/eventos/guardar",
        data=form_data,
        allow_redirects=True
    )

    assert submit_resp.status_code == 200, "❌ Error enviando el formulario"

    print("✅ Evento creado")

    # 5. Cargar listado
    print("➡️ Verificando que el evento aparece en /eventos ...")
    list_page = session.get(f"{BASE_URL}/eventos")
    assert list_page.status_code == 200, "❌ No se pudo cargar /eventos"

    assert "Evento de Prueba" in list_page.text, "❌ El evento no aparece en el listado"

    print("✅ El evento aparece en el listado correctamente")
