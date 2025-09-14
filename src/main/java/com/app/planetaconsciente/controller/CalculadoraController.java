package com.app.planetaconsciente.controller;

import com.app.planetaconsciente.model.Calculadora;
import com.app.planetaconsciente.repository.CalculadoraRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/calculadora")
public class CalculadoraController {

    @Autowired
    private CalculadoraRepository calculadoraRepository;

    @GetMapping
    public String mostrarCalculadora(
            @RequestParam(name = "pregunta", defaultValue = "0") int preguntaActual,
            HttpSession session,
            Model model) {

        List<Map<String, Object>> preguntas = obtenerPreguntasCompletas();

        if (preguntaActual >= preguntas.size()) {
            return "redirect:/calculadora/resultado";
        }

        model.addAttribute("preguntas", preguntas);
        model.addAttribute("preguntaActual", preguntaActual);
        return "calculadora/index";
    }

    @PostMapping("/responder")
    public String procesarRespuesta(
            @RequestParam("respuesta") String respuesta,
            @RequestParam("preguntaActual") int preguntaActual,
            HttpSession session) {

        List<Map<String, Object>> preguntas = obtenerPreguntasCompletas();
        Map<Integer, Object> respuestas = obtenerRespuestasDeSesion(session);

        Map<String, Object> pregunta = preguntas.get(preguntaActual);
        Object respuestaProcesada = procesarRespuesta(respuesta, pregunta);

        respuestas.put(preguntaActual, respuestaProcesada);
        session.setAttribute("respuestas", respuestas);

        return (preguntaActual < preguntas.size() - 1) ? "redirect:/calculadora?pregunta=" + (preguntaActual + 1)
                : "redirect:/calculadora/resultado";
    }

    @GetMapping("/resultado")
    public String mostrarResultado(HttpSession session, Model model) {
        Map<Integer, Object> respuestas = obtenerRespuestasDeSesion(session);
        if (respuestas.isEmpty()) {
            return "redirect:/calculadora";
        }

        double huella = calcularHuellaCarbono(respuestas);
        String clasificacion = determinarClasificacion(huella);

        guardarResultadoEnBD(respuestas, huella, clasificacion);

        // Cambio principal aquí: Pasamos el valor numérico directamente
        model.addAttribute("huella", huella);
        model.addAttribute("clasificacion", clasificacion);
        return "calculadora/resultado";
    }

    private List<Map<String, Object>> obtenerPreguntasCompletas() {
        List<Map<String, Object>> preguntas = new ArrayList<>();

        agregarPreguntaNumerica(preguntas, "¿Cuál es tu edad?", 1, 120, "años");
        agregarPreguntaSeleccion(preguntas, "¿Con qué género te identificas?",
                Map.of("M", "Masculino", "F", "Femenino", "O", "Otro", "P", "Prefiero no decir"));
        agregarPreguntaSeleccion(preguntas, "¿Cuál es tu principal medio de transporte?",
                Map.of("0", "No tengo vehículo", "1", "Automóvil", "2", "Bicicleta",
                        "3", "Transporte público", "4", "Caminando", "5", "Motocicleta"));
        agregarPreguntaSeleccion(preguntas, "¿Qué tipo de combustible usa tu vehículo principal?",
                Map.of("0", "Gasolina", "1", "Diésel", "2", "Eléctrico/Híbrido",
                        "3", "No aplica", "4", "Gas natural"));
        agregarPreguntaNumerica(preguntas,
                "¿Cuántos kilómetros recorres en promedio cada día en automóvil?(0 si no aplica)", 0, 500, "km/día");
        agregarPreguntaNumerica(preguntas,
                "¿Cuántos kilómetros recorres en promedio cada día en bicicleta?(0 si no aplica)", 0, 200, "km/día");
        agregarPreguntaSeleccion(preguntas, "¿Con qué frecuencia usas transporte público?",
                Map.of("0", "Nunca", "1", "Ocasionalmente (1-2 veces/semana)",
                        "2", "Frecuentemente (3-5 veces/semana)", "3", "Diariamente",
                        "4", "Varias veces al día"));
        agregarPreguntaNumerica(preguntas, "¿Cuántos vuelos de corta distancia (menos de 3 horas) realizas al año?(0 si no aplica)", 0,
                50, "vuelos/año");
        agregarPreguntaNumerica(preguntas, "¿Cuántos vuelos de larga distancia (más de 3 horas) realizas al año?(0 si no aplica)", 0,
                20, "vuelos/año");
        agregarPreguntaNumerica(preguntas, "¿Cuál es tu consumo eléctrico mensual aproximado?", 0, 2000, "kWh/mes");
        agregarPreguntaSeleccion(preguntas, "¿Qué tipo de energía usas principalmente en tu hogar?",
                Map.of("0", "Energía convencional (no renovable)", "1", "Energía renovable (solar, eólica, etc.)",
                        "2", "Mixta", "3", "No sé"));
        agregarPreguntaNumerica(preguntas, "¿Cuántas bolsas de basura de tamaño estándar (50L) generas por semana?", 0,
                20, "bolsas/semana");
        agregarPreguntaNumerica(preguntas, "¿Qué porcentaje aproximado de tus residuos reciclas o compostas?(0% no reciclas 100% reciclas demaciado)", 0, 100,
                "%");
        agregarPreguntaNumerica(preguntas, "¿Cuál es tu consumo diario aproximado de agua?", 0, 1000, "litros/día");
        agregarPreguntaSeleccion(preguntas, "¿Qué tipo de dieta sigues principalmente?",
                Map.of("0", "Carnívora (carne en todas las comidas)", "1", "Omnívora equilibrada",
                        "2", "Vegetariana", "3", "Vegana", "4", "Pescetariana"));

        return preguntas;
    }

    private void agregarPreguntaNumerica(List<Map<String, Object>> preguntas,
            String texto, int min, int max, String unidad) {
        Map<String, Object> pregunta = new LinkedHashMap<>();
        pregunta.put("texto", texto);
        pregunta.put("tipo", "numero");
        pregunta.put("min", min);
        pregunta.put("max", max);
        pregunta.put("unidad", unidad);
        preguntas.add(pregunta);
    }

    private void agregarPreguntaSeleccion(List<Map<String, Object>> preguntas,
            String texto, Map<?, ?> opciones) {
        Map<String, Object> pregunta = new LinkedHashMap<>();
        pregunta.put("texto", texto);
        pregunta.put("tipo", "seleccion");
        pregunta.put("opciones", opciones);
        preguntas.add(pregunta);
    }

    private Map<Integer, Object> obtenerRespuestasDeSesion(HttpSession session) {
        @SuppressWarnings("unchecked")
        Map<Integer, Object> respuestas = (Map<Integer, Object>) session.getAttribute("respuestas");
        return respuestas != null ? new HashMap<>(respuestas) : new HashMap<>();
    }

    private Object procesarRespuesta(String respuesta, Map<String, Object> pregunta) {
        try {
            return "numero".equals(pregunta.get("tipo")) ? Double.parseDouble(respuesta) : respuesta;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private double calcularHuellaCarbono(Map<Integer, Object> respuestas) {
        double huella = 0;
        Map<Integer, Object> factores = obtenerFactoresDeEmision();

        for (Map.Entry<Integer, Object> entry : respuestas.entrySet()) {
            int indice = entry.getKey();
            Object valor = entry.getValue();

            if (factores.containsKey(indice)) {
                Object factorObj = factores.get(indice);

                if (factorObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<Object, Double> factorMap = (Map<Object, Double>) factorObj;
                    huella += factorMap.getOrDefault(valor, 0.0);
                } else if (factorObj instanceof Double) {
                    double factor = (Double) factorObj;
                    if (valor instanceof Number) {
                        huella += ((Number) valor).doubleValue() * factor;
                    }
                }
            }
        }
        return huella;
    }

    private Map<Integer, Object> obtenerFactoresDeEmision() {
        Map<Integer, Object> factores = new HashMap<>();

        factores.put(2, Map.of("0", 0.0, "1", 0.25, "2", 0.005, "3", 0.05, "4", 0.0, "5", 0.15));
        factores.put(3, Map.of("0", 2.31, "1", 2.68, "2", 0.12, "3", 0.0, "4", 1.5));
        factores.put(4, 0.25);
        factores.put(5, 0.005);
        factores.put(6, Map.of("0", 0.0, "1", 500.0, "2", 1200.0, "3", 2500.0, "4", 4000.0));
        factores.put(7, 250.0);
        factores.put(8, 1000.0);
        factores.put(9, 0.5);
        factores.put(10, Map.of("0", 1.0, "1", 0.1, "2", 0.5, "3", 0.7));
        factores.put(11, 5.0);
        factores.put(12, -0.02);
        factores.put(13, 0.001);
        factores.put(14, Map.of("0", 3000.0, "1", 2000.0, "2", 1500.0, "3", 1000.0, "4", 1800.0));

        return factores;
    }

    private String determinarClasificacion(double huella) {
        if (huella < 3000)
            return "Baja (Ecológica)";
        if (huella <= 6000)
            return "Media (Promedio)";
        if (huella <= 10000)
            return "Alta (Necesita mejorar)";
        return "Muy Alta (Impacto significativo)";
    }

    private void guardarResultadoEnBD(Map<Integer, Object> respuestas, double huella, String clasificacion) {
        Calculadora calculadora = new Calculadora();

        try {
            // Mapeo seguro de respuestas a campos del modelo
            if (respuestas.get(0) != null)
                calculadora.setEdad(((Number) respuestas.get(0)).intValue());
            if (respuestas.get(1) != null)
                calculadora.setSexo(respuestas.get(1).toString());
            if (respuestas.get(2) != null)
                calculadora.setMedioTransporte(Integer.parseInt(respuestas.get(2).toString()));
            if (respuestas.get(3) != null)
                calculadora.setTipoCombustible(Integer.parseInt(respuestas.get(3).toString()));
            if (respuestas.get(4) != null)
                calculadora.setKmAutomovilDia(((Number) respuestas.get(4)).doubleValue());
            if (respuestas.get(5) != null)
                calculadora.setKmBicicletaDia(((Number) respuestas.get(5)).doubleValue());
            if (respuestas.get(6) != null)
                calculadora.setFrecuenciaTransportePublico(Integer.parseInt(respuestas.get(6).toString()));
            if (respuestas.get(7) != null)
                calculadora.setVuelosCortosAnuales(((Number) respuestas.get(7)).intValue());
            if (respuestas.get(8) != null)
                calculadora.setVuelosLargosAnuales(((Number) respuestas.get(8)).intValue());
            if (respuestas.get(9) != null)
                calculadora.setConsumoElectricidad(((Number) respuestas.get(9)).doubleValue());
            if (respuestas.get(10) != null)
                calculadora.setTipoEnergia(Integer.parseInt(respuestas.get(10).toString()));
            if (respuestas.get(11) != null)
                calculadora.setBolsasBasura(((Number) respuestas.get(11)).intValue());
            if (respuestas.get(12) != null)
                calculadora.setPorcentajeReciclaje(((Number) respuestas.get(12)).doubleValue());
            if (respuestas.get(13) != null)
                calculadora.setConsumoAgua(((Number) respuestas.get(13)).doubleValue());
            if (respuestas.get(14) != null)
                calculadora.setTipoDieta(Integer.parseInt(respuestas.get(14).toString()));

            calculadora.setResultado(huella);
            calculadora.setClasificacion(clasificacion);
            calculadora.setFechaCreacion(LocalDateTime.now());

            calculadoraRepository.save(calculadora);
        } catch (Exception e) {
            System.err.println("Error al guardar resultado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}