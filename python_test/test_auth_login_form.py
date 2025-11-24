import requests
from bs4 import BeautifulSoup
import re

BASE_URL = "http://localhost:8070"

def extraer_csrf(html):
    """Extrae el token CSRF desde cualquier formulario."""
    match = re.search(r'name="_csrf"\s+value="([^"]+)"', html)
    assert match, "❌ No se encontró el CSRF en el formulario"
    return match.group(1)

def test_auth_login_form():
    print("➡️ Cargando /login ...")
    session = requests.Session()

    # 1. Cargar la página de login
    response = session.get(f"{BASE_URL}/login")
    assert response.status_code == 200, "❌ No se pudo cargar la vista /login"

    print("✅ Página cargó OK")

    soup = BeautifulSoup(response.text, "html.parser")

    # 2. Validar que existe formulario de login
    form = soup.find("form")
    assert form is not None, "❌ No se encontró ningún <form> en la página de login"

    # 3. Validar presencia de username
    user_field = soup.find("input", {"name": "username"})
    assert user_field is not None, "❌ No existe el campo 'username' en el login"

    # 4. Validar presencia de password
    pass_field = soup.find("input", {"name": "password"})
    assert pass_field is not None, "❌ No existe el campo 'password' en el login"

    # 5. Validar CSRF token
    csrf_field = soup.find("input", {"name": "_csrf"})
    assert csrf_field is not None, "❌ No se encontró el token _csrf"

    print("✅ Formulario, campos y CSRF presentes")

def test_auth_login_with_parameters():
    print("➡️ Probando parámetros opcionales en /login?success ...")
    session = requests.Session()

    # 1. Probar parámetro ?success
    resp_success = session.get(f"{BASE_URL}/login?success")
    assert resp_success.status_code == 200, "❌ /login?success no cargó correctamente"

    # 2. Probar parámetro ?verified
    resp_verified = session.get(f"{BASE_URL}/login?verified")
    assert resp_verified.status_code == 200, "❌ /login?verified no cargó correctamente"

    # 3. Probar parámetro ?resetSent
    resp_reset = session.get(f"{BASE_URL}/login?resetSent")
    assert resp_reset.status_code == 200, "❌ /login?resetSent no cargó correctamente"

    # 4. Probar parámetro ?passwordChanged
    resp_changed = session.get(f"{BASE_URL}/login?passwordChanged")
    assert resp_changed.status_code == 200, "❌ /login?passwordChanged no cargó correctamente"

    print("✅ Parámetros opcionales funcionan correctamente")
