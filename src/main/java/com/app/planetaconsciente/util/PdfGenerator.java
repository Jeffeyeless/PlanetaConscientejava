package com.app.planetaconsciente.util;

import com.app.planetaconsciente.model.Noticia;
import com.app.planetaconsciente.model.Calculadora;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
public class PdfGenerator {

    public byte[] generateNoticiasPdf(List<Noticia> noticias, String busqueda, String fuente, String fechaDesde, String fechaHasta) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            
            // Título con logo local (si existe)
            try {
                String logoPath = "src/main/resources/static/images/1.jpg";
                File logoFile = new File(logoPath);
                if (logoFile.exists()) {
                    Image logo = Image.getInstance(logoFile.getAbsolutePath());
                    logo.scaleToFit(80, 80);
                    logo.setAlignment(Element.ALIGN_CENTER);
                    document.add(logo);
                }
            } catch (Exception e) {
                // Si no se puede cargar el logo, continuar sin él
            }

            // Título
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Reporte de Noticias", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            // Filtros aplicados
            if (busqueda != null || fuente != null || fechaDesde != null || fechaHasta != null) {
                Font filterFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
                StringBuilder filters = new StringBuilder("Filtros aplicados: ");
                
                if (busqueda != null) filters.append("Búsqueda: ").append(busqueda).append(" ");
                if (fuente != null) filters.append("Fuente: ").append(fuente).append(" ");
                if (fechaDesde != null) filters.append("Desde: ").append(fechaDesde).append(" ");
                if (fechaHasta != null) filters.append("Hasta: ").append(fechaHasta);
                
                Paragraph filterParagraph = new Paragraph(filters.toString(), filterFont);
                filterParagraph.setSpacingAfter(15);
                document.add(filterParagraph);
            }
            
            // Contenido
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            
            for (Noticia noticia : noticias) {
                Paragraph noticiaTitle = new Paragraph(noticia.getTitulo(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
                noticiaTitle.setSpacingAfter(5);

                // Agregar imagen si existe imagenUrl
                if (noticia.getImagenUrl() != null && !noticia.getImagenUrl().isEmpty()) {
                    try {
                        Image img = Image.getInstance(new URL(noticia.getImagenUrl()));
                        img.scaleToFit(350, 200);
                        img.setAlignment(Element.ALIGN_CENTER);
                        document.add(img);
                    } catch (Exception e) {
                        // Si la imagen no se puede cargar, ignorar y continuar
                    }
                }

                Paragraph noticiaMeta = new Paragraph(
                    noticia.getFechaPublicacion().format(dateFormatter) + 
                    (noticia.getFuente() != null ? " | " + noticia.getFuente() : ""),
                    contentFont
                );
                noticiaMeta.setSpacingAfter(10);

                Paragraph noticiaContent = new Paragraph(noticia.getResumen(), contentFont);
                noticiaContent.setSpacingAfter(15);

                document.add(noticiaTitle);
                document.add(noticiaMeta);
                document.add(noticiaContent);
                document.add(new Paragraph("----------------------------------------------------"));
            }
            
            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        
        return out.toByteArray();
    }

    public byte[] generateEstadisticasPdf(Map<String, Object> estadisticas, LocalDate fechaDesde, LocalDate fechaHasta) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Título
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Reporte de Estadísticas - Huella de Carbono", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Fecha de generación
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Paragraph fechaGeneracion = new Paragraph("Generado el: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), dateFont);
            fechaGeneracion.setAlignment(Element.ALIGN_RIGHT);
            fechaGeneracion.setSpacingAfter(10);
            document.add(fechaGeneracion);

            // Filtros aplicados
            if (fechaDesde != null || fechaHasta != null) {
                Font filterFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
                StringBuilder filtroTexto = new StringBuilder("Filtros aplicados: ");
                if (fechaDesde != null) filtroTexto.append("Desde: ").append(fechaDesde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                if (fechaHasta != null) filtroTexto.append(" Hasta: ").append(fechaHasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                
                Paragraph filtros = new Paragraph(filtroTexto.toString(), filterFont);
                filtros.setSpacingAfter(10);
                document.add(filtros);
            }

            // Resumen general
            Long totalRegistros = (Long) estadisticas.get("totalRegistros");
            Font summaryFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Paragraph summary = new Paragraph("Total de registros analizados: " + totalRegistros, summaryFont);
            summary.setSpacingAfter(15);
            document.add(summary);

            // Distribución por clasificación
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Paragraph section1 = new Paragraph("Distribución por Clasificación de Huella", sectionFont);
            section1.setSpacingAfter(10);
            document.add(section1);

            // Tabla de clasificación
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            table.setSpacingAfter(15);

            // Encabezados de tabla
            table.addCell(new Phrase("Clasificación", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            table.addCell(new Phrase("Cantidad", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            table.addCell(new Phrase("Porcentaje", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            table.addCell(new Phrase("Promedio Huella", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));

            @SuppressWarnings("unchecked")
            Map<String, Integer> distribucion = (Map<String, Integer>) estadisticas.get("distribucionClasificacion");
            @SuppressWarnings("unchecked")
            Map<String, Double> porcentajes = (Map<String, Double>) estadisticas.get("porcentajeClasificacion");
            @SuppressWarnings("unchecked")
            Map<String, Double> promedios = (Map<String, Double>) estadisticas.get("promedioHuella");

            if (distribucion != null && !distribucion.isEmpty()) {
                for (String clasificacion : distribucion.keySet()) {
                    table.addCell(clasificacion);
                    table.addCell(String.valueOf(distribucion.get(clasificacion)));
                    table.addCell(porcentajes.get(clasificacion) + "%");
                    table.addCell(promedios.get(clasificacion) + " kg CO₂");
                }
            } else {
                table.addCell("No hay datos");
                table.addCell("0");
                table.addCell("0%");
                table.addCell("0 kg CO₂");
            }

            document.add(table);

            // Distribución por género
            Paragraph section2 = new Paragraph("Distribución por Género", sectionFont);
            section2.setSpacingAfter(10);
            document.add(section2);

            PdfPTable generoTable = new PdfPTable(2);
            generoTable.setWidthPercentage(100);
            generoTable.setSpacingBefore(10);
            generoTable.setSpacingAfter(15);

            generoTable.addCell(new Phrase("Género", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            generoTable.addCell(new Phrase("Cantidad", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));

            @SuppressWarnings("unchecked")
            List<Object[]> generoStats = (List<Object[]>) estadisticas.get("generoStats");
            
            if (generoStats != null && !generoStats.isEmpty()) {
                for (Object[] stat : generoStats) {
                    String genero = getGeneroDisplayName((String) stat[0]);
                    Long count = (Long) stat[1];
                    generoTable.addCell(genero);
                    generoTable.addCell(String.valueOf(count));
                }
            } else {
                generoTable.addCell("No hay datos");
                generoTable.addCell("0");
            }

            document.add(generoTable);

            // Top 5 mayores huellas
            Paragraph section3 = new Paragraph("Top 5 Mayores Huellas de Carbono", sectionFont);
            section3.setSpacingAfter(10);
            document.add(section3);

            @SuppressWarnings("unchecked")
            List<Calculadora> topMayoresHuellas = (List<Calculadora>) estadisticas.get("topMayoresHuellas");
            
            if (topMayoresHuellas != null && !topMayoresHuellas.isEmpty()) {
                PdfPTable topTable = new PdfPTable(3);
                topTable.setWidthPercentage(100);
                topTable.setSpacingBefore(10);

                topTable.addCell(new Phrase("#", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
                topTable.addCell(new Phrase("Huella (kg CO₂)", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
                topTable.addCell(new Phrase("Clasificación", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));

                int rank = 1;
                for (Calculadora calculadora : topMayoresHuellas) {
                    topTable.addCell(String.valueOf(rank));
                    topTable.addCell(String.format("%.2f", calculadora.getResultado()));
                    topTable.addCell(calculadora.getClasificacion());
                    rank++;
                }
                document.add(topTable);
            } else {
                Paragraph noData = new Paragraph("No hay datos de huellas registradas", FontFactory.getFont(FontFactory.HELVETICA, 10));
                document.add(noData);
            }

            document.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF de estadísticas: " + e.getMessage(), e);
        }
    }

    private String getGeneroDisplayName(String generoCode) {
        if (generoCode == null) return "No especificado";
        switch (generoCode) {
            case "M": return "Masculino";
            case "F": return "Femenino";
            case "O": return "Otro";
            case "P": return "Prefiero no decir";
            default: return generoCode;
        }
    }
}