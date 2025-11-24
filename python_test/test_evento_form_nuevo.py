import requests
from bs4 import BeautifulSoup
import re

BASE_URL = "http://localhost:8070"

def extraer_csrf(html):
    """Extrae token CSRF desde el formulario."""
    match = re.search(r'name="_csrf"\s+value="([^"]+)"', html)
    assert match, "❌ No se encontró el CSRF en el formulario"
    return match.group(1)

def test_evento_formulario_nuevo():
    session = requests.Session()

    print("➡️ Abriendo página /login ...")

    # 1. Obtener login page
    login_page = session.get(f"{BASE_URL}/login")
    assert login_page.status_code == 200, "❌ No se pudo cargar /login"

    csrf_login = extraer_csrf(login_page.text)

    # 2. Iniciar sesión
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
    resp = session.get(f"{BASE_URL}/eventos/nuevo")
    assert resp.status_code == 200, "❌ No se pudo cargar /eventos/nuevo"

    soup = BeautifulSoup(resp.text, "html.parser")

    # 4. Validar que existe el formulario
    form = soup.find("form")
    assert form is not None, "❌ No se encontró el formulario en la vista"

    # 5. Validar que exista campo título
    titulo_input = soup.find("input", {"name": "titulo"})
    assert titulo_input is not None, "❌ El formulario no tiene campo 'titulo'"

    # 6. Validar que exista campo descripción
    descripcion = soup.find("textarea", {"name": "descripcion"})
    assert descripcion is not None, "❌ El formulario no tiene campo 'descripcion'"

    # 7. Validar CSRF
    csrf_field = soup.find("input", {"name": "_csrf"})
    assert csrf_field is not None, "❌ No hay input CSRF en el formulario"

    print("✅ Formulario /eventos/nuevo cargado correctamente y con todos los campos requeridos")
