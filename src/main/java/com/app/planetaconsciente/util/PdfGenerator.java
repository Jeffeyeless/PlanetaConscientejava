package com.app.planetaconsciente.util;

import com.app.planetaconsciente.model.Noticia;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
}