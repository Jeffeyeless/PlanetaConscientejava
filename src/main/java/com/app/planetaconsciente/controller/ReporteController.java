package com.app.planetaconsciente.controller;

import com.app.planetaconsciente.model.Calculadora;
import com.app.planetaconsciente.repository.CalculadoraReporteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    @Autowired
    private CalculadoraReporteRepository calculadoraReporteRepository;

    @GetMapping("/estadisticas")
    public String mostrarEstadisticas(Model model) {
        try {
            System.out.println("=== INICIANDO REPORTE ESTADÍSTICAS ===");
            
            // Estadísticas principales
            Long totalRegistros = calculadoraReporteRepository.getTotalRegistros();
            System.out.println("Total registros: " + totalRegistros);
            model.addAttribute("totalRegistros", totalRegistros);
            
            // Verificar si hay datos
            if (totalRegistros == 0) {
                System.out.println("No hay datos en la base de datos");
                model.addAttribute("sinDatos", true);
                return "reportes/estadisticas";
            }

            // Distribución por clasificación
            List<Object[]> clasificacionStats = calculadoraReporteRepository.countByClasificacion();
            System.out.println("Clasificaciones encontradas: " + clasificacionStats.size());
            
            Map<String, Integer> distribucionClasificacion = new HashMap<>();
            Map<String, Double> porcentajeClasificacion = new HashMap<>();
            
            for (Object[] stat : clasificacionStats) {
                String clasificacion = (String) stat[0];
                Long count = (Long) stat[1];
                distribucionClasificacion.put(clasificacion, count.intValue());
                
                // Calcular porcentaje
                double porcentaje = (count.doubleValue() / totalRegistros) * 100;
                porcentajeClasificacion.put(clasificacion, Math.round(porcentaje * 100.0) / 100.0);
                System.out.println("Clasificación: " + clasificacion + " - Cantidad: " + count + " - Porcentaje: " + porcentaje);
            }
            
            model.addAttribute("distribucionClasificacion", distribucionClasificacion);
            model.addAttribute("porcentajeClasificacion", porcentajeClasificacion);

            // Promedio de huella por clasificación
            List<Object[]> avgHuellaStats = calculadoraReporteRepository.avgHuellaByClasificacion();
            Map<String, Double> promedioHuella = new HashMap<>();
            
            for (Object[] stat : avgHuellaStats) {
                String clasificacion = (String) stat[0];
                Double avg = (Double) stat[1];
                promedioHuella.put(clasificacion, Math.round(avg * 100.0) / 100.0);
                System.out.println("Promedio " + clasificacion + ": " + avg);
            }
            
            model.addAttribute("promedioHuella", promedioHuella);
            
            // Distribución por género
            List<Object[]> generoStats = calculadoraReporteRepository.countBySexo();
            System.out.println("Estadísticas de género encontradas: " + generoStats.size());

            Map<String, Long> distribucionGenero = new HashMap<>();
            for (Object[] stat : generoStats) {
                String generoCode = (String) stat[0];
                Long count = (Long) stat[1];
                distribucionGenero.put(generoCode, count);
                System.out.println("Género: " + generoCode + " - Cantidad: " + count);
            }

            model.addAttribute("generoStats", distribucionGenero);

            // Top 5 mayores huellas - CORREGIDO: ahora usa List<Calculadora>
            List<Calculadora> topMayoresHuellas = calculadoraReporteRepository.findTop5MayoresHuellas();
            System.out.println("Top 5 huellas encontradas: " + topMayoresHuellas.size());
            
            // Log para verificar los datos
            for (Calculadora calc : topMayoresHuellas) {
                System.out.println("Top huella - ID: " + calc.getId() + ", Resultado: " + calc.getResultado());
            }
            
            model.addAttribute("topMayoresHuellas", topMayoresHuellas);

            System.out.println("=== REPORTE COMPLETADO ===");
            return "reportes/estadisticas";
            
        } catch (Exception e) {
            System.err.println("ERROR en reporte: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error al cargar las estadísticas: " + e.getMessage());
            model.addAttribute("totalRegistros", 0L);
            return "reportes/estadisticas";
        }
    }
}