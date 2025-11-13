#!/usr/bin/env python3
"""
PRUEBA UNITARIA - Endpoint Público - PUERTO 8070
Ejecutar:
1. mvn spring-boot:run
2. python test_clasificacion_public.py
"""

import requests
import time

# ⚠️ CONFIGURACIÓN - CAMBIA ESTO SI TU PUERTO ES DIFERENTE
PUERTO = "8070"
URL_BASE = f"http://localhost:{PUERTO}"

def probar_clasificacion(huella):
    """
    Llama al endpoint público /api/public/clasificacion
    """
    url = f"{URL_BASE}/api/public/clasificacion"
    
    try:
        response = requests.get(url, params={"huella": huella}, timeout=10)
        
        if response.status_code == 200:
            return response.text.strip()
        else:
            return f"ERROR: HTTP {response.status_code} - {response.text}"
            
    except requests.exceptions.ConnectionError:
        return "ERROR: No se puede conectar al servidor. ¿Spring Boot está ejecutándose?"
    except Exception as e:
        return f"ERROR: {str(e)}"

def verificar_endpoint():
    """Verifica que el endpoint esté funcionando"""
    print(f"🔍 Verificando endpoint público (puerto {PUERTO})...")
    
    # Probar el health check primero
    try:
        health_url = f"{URL_BASE}/api/public/health"
        response = requests.get(health_url, timeout=5)
        if response.status_code == 200:
            print("✅ Endpoint público funcionando correctamente")
            return True
        else:
            print(f"❌ Health check falló: {response.status_code}")
            return False
    except Exception as e:
        print(f"❌ No se puede conectar al endpoint público: {str(e)}")
        return False

def prueba_completa():
    """Prueba completa con todos los casos"""
    print("\n🚀 INICIANDO PRUEBAS UNITARIAS COMPLETAS")
    print("=" * 55)
    
    # Todos los casos de prueba
    test_cases = [
        (0, "Baja (Ecológica)"),
        (1500, "Baja (Ecológica)"),
        (2999, "Baja (Ecológica)"),
        (2999.99, "Baja (Ecológica)"),
        (3000, "Media (Promedio)"),
        (4500, "Media (Promedio)"),
        (6000, "Media (Promedio)"),
        (6000.01, "Alta (Necesita mejorar)"),
        (6001, "Alta (Necesita mejorar)"),
        (8000, "Alta (Necesita mejorar)"),
        (10000, "Alta (Necesita mejorar)"),
        (10000.01, "Muy Alta (Impacto significativo)"),
        (10001, "Muy Alta (Impacto significativo)"),
        (15000, "Muy Alta (Impacto significativo)"),
        (50000, "Muy Alta (Impacto significativo)")
    ]
    
    print("\n🧪 PROBANDO TODAS LAS CLASIFICACIONES:")
    print("-" * 50)
    
    exitosos = 0
    totales = len(test_cases)
    
    for huella, esperado in test_cases:
        resultado = probar_clasificacion(huella)
        if resultado == esperado:
            print(f"✅ {str(huella):10} → '{resultado}'")
            exitosos += 1
        else:
            print(f"❌ {str(huella):10} → Esperado: '{esperado}'")
            print(f"{' ':12}  Obtenido: '{resultado}'")
    
    print("-" * 50)
    print(f"📊 RESULTADO: {exitosos}/{totales} pruebas exitosas")
    
    if exitosos == totales:
        print("🎉 ¡TODAS LAS PRUEBAS PASARON!")
        return True
    else:
        print(f"💡 {totales - exitosos} pruebas fallaron")
        return False

def prueba_rapida():
    """Prueba rápida con valores clave"""
    print("\n🔍 PRUEBA RÁPIDA (valores clave):")
    print("-" * 35)
    
    valores_clave = [0, 3000, 6001, 10001]
    for valor in valores_clave:
        resultado = probar_clasificacion(valor)
        estado = "✅" if resultado in ["Baja (Ecológica)", "Media (Promedio)", "Alta (Necesita mejorar)", "Muy Alta (Impacto significativo)"] else "❌"
        print(f"   {estado} {valor:6} → {resultado}")

def probar_url_directamente():
    """Prueba las URLs directamente en el navegador"""
    print("\n🌐 URLs PARA PROBAR MANUALMENTE:")
    print("-" * 40)
    print(f"🔗 Health Check: {URL_BASE}/api/public/health")
    print(f"🔗 Clasificación 0: {URL_BASE}/api/public/clasificacion?huella=0")
    print(f"🔗 Clasificación 3000: {URL_BASE}/api/public/clasificacion?huella=3000")
    print(f"🔗 Clasificación 10001: {URL_BASE}/api/public/clasificacion?huella=10001")

def main():
    """Función principal"""
    print("🌱 PRUEBAS UNITARIAS - PLANETA CONSCIENTE")
    print(f"📡 Usando endpoint público: http://localhost:{PUERTO}/api/public/clasificacion")
    print("=" * 60)
    
    # Esperar un poco para que Spring Boot esté listo
    print("⏳ Esperando que Spring Boot esté listo...")
    time.sleep(3)
    
    # Verificar que el endpoint esté funcionando
    if not verificar_endpoint():
        print("\n❌ El endpoint público no está disponible")
        print("💡 Soluciones:")
        print("   1. Verifica que creaste TestPublicController.java")
        print("   2. Asegúrate de que Spring Security permite /api/public/**")
        print("   3. Reinicia Spring Boot completamente")
        print("   4. Ejecuta: mvn compile")
        
        # Mostrar URLs para probar manualmente
        probar_url_directamente()
        return
    
    # Ejecutar prueba rápida primero
    prueba_rapida()
    
    # Preguntar si quiere hacer prueba completa
    print("\n" + "=" * 60)
    respuesta = input("¿Quieres ejecutar la prueba COMPLETA? (s/n): ")
    
    if respuesta.lower() == 's':
        print("")
        if prueba_completa():
            print("\n🎊 ¡FELICITACIONES! Tu método pasa todas las pruebas")
        else:
            print("\n🔧 Revisa los resultados fallidos arriba")
    else:
        print("\n👋 Prueba rápida completada")

if __name__ == '__main__':
    main()