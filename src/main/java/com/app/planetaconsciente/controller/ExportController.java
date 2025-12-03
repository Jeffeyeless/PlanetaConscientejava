package com.app.planetaconsciente.controller;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import com.app.planetaconsciente.model.Noticia;
import com.app.planetaconsciente.model.Calculadora;
import com.app.planetaconsciente.service.NoticiaService;
import com.app.planetaconsciente.dto.FiltroNoticia;
import com.app.planetaconsciente.repository.CalculadoraReporteRepository;
import com.app.planetaconsciente.util.PdfGenerator;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

        FiltroNoticia filtro = new FiltroNoticia();
        filtro.setBusqueda(busqueda);
        filtro.setFuente(fuente);
        filtro.setFechaDesde(fechaDesde);
        filtro.setFechaHasta(fechaHasta);
        filtro.setPage(0);
        filtro.setSize(Integer.MAX_VALUE);

        List<Noticia> noticiasFiltradas = noticiaService
                .filtrar(filtro)
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

    @GetMapping("/exportar/noticias/excel")
    public void exportarNoticiasExcel(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String fuente,
            @RequestParam(required = false, name = "fecha_desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false, name = "fecha_hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            HttpServletResponse response) throws IOException {

        FiltroNoticia filtro = new FiltroNoticia();
        filtro.setBusqueda(busqueda);
        filtro.setFuente(fuente);
        filtro.setFechaDesde(fechaDesde);
        filtro.setFechaHasta(fechaHasta);
        filtro.setPage(0);
        filtro.setSize(Integer.MAX_VALUE);

        List<Noticia> noticiasFiltradas = noticiaService
                .filtrar(filtro)
                .getContent();

        // Crear Excel
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Noticias");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Título");
        header.createCell(1).setCellValue("Resumen");
        header.createCell(2).setCellValue("Fuente");
        header.createCell(3).setCellValue("Fecha");

        int rowIndex = 1;
        for (Noticia n : noticiasFiltradas) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(n.getTitulo());
            row.createCell(1).setCellValue(n.getResumen());
            row.createCell(2).setCellValue(n.getFuente());
            row.createCell(3).setCellValue(n.getFechaPublicacion().toString());
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=noticias.xlsx");

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/exportar/noticias/csv")
    public void exportarNoticiasCsv(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String fuente,
            @RequestParam(required = false, name = "fecha_desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false, name = "fecha_hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            HttpServletResponse response) throws IOException {

        FiltroNoticia filtro = new FiltroNoticia();
        filtro.setBusqueda(busqueda);
        filtro.setFuente(fuente);
        filtro.setFechaDesde(fechaDesde);
        filtro.setFechaHasta(fechaHasta);
        filtro.setPage(0);
        filtro.setSize(Integer.MAX_VALUE);

        List<Noticia> noticiasFiltradas = noticiaService
                .filtrar(filtro)
                .getContent();

        StringBuilder csv = new StringBuilder();
        csv.append("Titulo,Resumen,Fuente,Fecha\n");

        for (Noticia n : noticiasFiltradas) {
            csv.append("\"").append(n.getTitulo()).append("\",")
            .append("\"").append(n.getResumen()).append("\",")
            .append("\"").append(n.getFuente()).append("\",")
            .append(n.getFechaPublicacion()).append("\n");
        }

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=noticias.csv");

        response.getWriter().write(csv.toString());
        response.getWriter().flush();
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