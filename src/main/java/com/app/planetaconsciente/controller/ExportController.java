package com.app.planetaconsciente.controller;

import com.app.planetaconsciente.model.Noticia;
import com.app.planetaconsciente.model.Calculadora;
import com.app.planetaconsciente.service.NoticiaService;
import com.app.planetaconsciente.repository.CalculadoraReporteRepository;
import com.app.planetaconsciente.util.PdfGenerator;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.data.domain.Pageable.unpaged;

@Controller
public class ExportController {

    @Autowired
    private NoticiaService noticiaService;

    @Autowired
    private CalculadoraReporteRepository calculadoraReporteRepository;

    @Autowired
    private PdfGenerator pdfGenerator;

    @GetMapping("/exportar/noticias/pdf")
    public void exportarNoticiasPdf(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String fuente,
            @RequestParam(required = false, name = "fecha_desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false, name = "fecha_hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            HttpServletResponse response) throws IOException {

        List<Noticia> noticiasFiltradas = noticiaService
                .filtrarNoticias(busqueda, fuente, fechaDesde, fechaHasta, unpaged())
                .getContent();

        String fechaDesdeStr = (fechaDesde != null) ? fechaDesde.toString() : null;
        String fechaHastaStr = (fechaHasta != null) ? fechaHasta.toString() : null;

        byte[] pdfBytes = pdfGenerator.generateNoticiasPdf(noticiasFiltradas, busqueda, fuente, fechaDesdeStr,
                fechaHastaStr);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=noticias.pdf");
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }

    // NUEVO MÉTODO para exportar estadísticas de huella de carbono
    @GetMapping("/exportar/estadisticas/pdf")
    public void exportarEstadisticasPdf(
            @RequestParam(required = false, name = "fecha_desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false, name = "fecha_hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            HttpServletResponse response) throws IOException {

        // Obtener estadísticas
        Map<String, Object> estadisticas = obtenerEstadisticas(fechaDesde, fechaHasta);

        byte[] pdfBytes = pdfGenerator.generateEstadisticasPdf(estadisticas, fechaDesde, fechaHasta);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=estadisticas_huella_carbono.pdf");
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }

    private Map<String, Object> obtenerEstadisticas(LocalDate fechaDesde, LocalDate fechaHasta) {
        Map<String, Object> estadisticas = new HashMap<>();

        // Convertir LocalDate a LocalDateTime para la consulta
        LocalDateTime startDateTime = (fechaDesde != null) ? fechaDesde.atStartOfDay() : null;
        LocalDateTime endDateTime = (fechaHasta != null) ? fechaHasta.atTime(23, 59, 59) : null;

        // Obtener total de registros
        Long totalRegistros = (startDateTime != null && endDateTime != null) 
            ? calculadoraReporteRepository.countByFechaCreacionBetween(startDateTime, endDateTime)
            : calculadoraReporteRepository.getTotalRegistros();
        
        estadisticas.put("totalRegistros", totalRegistros);

        // Obtener distribución por clasificación
        List<Object[]> clasificacionStats = (startDateTime != null && endDateTime != null)
            ? calculadoraReporteRepository.countByClasificacionAndFecha(startDateTime, endDateTime)
            : calculadoraReporteRepository.countByClasificacion();
        
        Map<String, Integer> distribucionClasificacion = new HashMap<>();
        Map<String, Double> porcentajeClasificacion = new HashMap<>();
        
        for (Object[] stat : clasificacionStats) {
            String clasificacion = (String) stat[0];
            Long count = (Long) stat[1];
            distribucionClasificacion.put(clasificacion, count.intValue());
            
            if (totalRegistros > 0) {
                double porcentaje = (count.doubleValue() / totalRegistros) * 100;
                porcentajeClasificacion.put(clasificacion, Math.round(porcentaje * 100.0) / 100.0);
            } else {
                porcentajeClasificacion.put(clasificacion, 0.0);
            }
        }
        
        estadisticas.put("distribucionClasificacion", distribucionClasificacion);
        estadisticas.put("porcentajeClasificacion", porcentajeClasificacion);

        // Obtener promedio de huella por clasificación
        List<Object[]> avgHuellaStats = (startDateTime != null && endDateTime != null)
            ? calculadoraReporteRepository.avgHuellaByClasificacionAndFecha(startDateTime, endDateTime)
            : calculadoraReporteRepository.avgHuellaByClasificacion();
        
        Map<String, Double> promedioHuella = new HashMap<>();
        for (Object[] stat : avgHuellaStats) {
            String clasificacion = (String) stat[0];
            Double avg = (Double) stat[1];
            promedioHuella.put(clasificacion, Math.round(avg * 100.0) / 100.0);
        }
        
        estadisticas.put("promedioHuella", promedioHuella);
        
        // Obtener distribución por género
        List<Object[]> generoStats = (startDateTime != null && endDateTime != null)
            ? calculadoraReporteRepository.countBySexoAndFecha(startDateTime, endDateTime)
            : calculadoraReporteRepository.countBySexo();
        
        estadisticas.put("generoStats", generoStats);

        // Obtener top 5 mayores huellas
        List<Calculadora> topMayoresHuellas = (startDateTime != null && endDateTime != null)
            ? calculadoraReporteRepository.findTop5MayoresHuellasByFecha(startDateTime, endDateTime)
            : calculadoraReporteRepository.findTop5MayoresHuellas();
        
        estadisticas.put("topMayoresHuellas", topMayoresHuellas);

        return estadisticas;
    }
}