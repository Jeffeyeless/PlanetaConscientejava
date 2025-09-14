package com.app.planetaconsciente.controller;

import com.app.planetaconsciente.model.Noticia;
import com.app.planetaconsciente.service.NoticiaService;
import com.app.planetaconsciente.util.PdfGenerator;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.data.domain.Pageable.unpaged;

@Controller
public class ExportController {

    @Autowired
    private NoticiaService noticiaService;

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
}
