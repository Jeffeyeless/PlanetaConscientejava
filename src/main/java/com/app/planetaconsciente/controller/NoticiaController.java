package com.app.planetaconsciente.controller;

import com.app.planetaconsciente.model.Noticia;
import com.app.planetaconsciente.service.FileStorageService;
import com.app.planetaconsciente.service.NoticiaService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/noticias")
@RequiredArgsConstructor
public class NoticiaController {

    private final NoticiaService noticiaService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public String listarNoticias(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String fuente,
            @RequestParam(required = false, name = "fecha_desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false, name = "fecha_hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false, name = "generar_pdf") String generarPdf,
            Model model) {

        // Limpiar parámetros vacíos
        if (busqueda != null && busqueda.trim().isEmpty()) busqueda = null;
        if (fuente != null && fuente.trim().isEmpty()) fuente = null;

        // Generar PDF si se solicitó
        if ("1".equals(generarPdf)) {
            String url = UriComponentsBuilder.fromPath("/exportar/noticias/pdf")
                    .queryParamIfPresent("busqueda", Optional.ofNullable(busqueda))
                    .queryParamIfPresent("fuente", Optional.ofNullable(fuente))
                    .queryParamIfPresent("fecha_desde", Optional.ofNullable(fechaDesde))
                    .queryParamIfPresent("fecha_hasta", Optional.ofNullable(fechaHasta))
                    .build()
                    .toUriString();
            return "redirect:" + url;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("fechaPublicacion").descending());
        Page<Noticia> noticias;

        // ✅ Validación: fechaDesde no puede ser mayor que fechaHasta
        if (fechaDesde != null && fechaHasta != null && fechaDesde.isAfter(fechaHasta)) {
            model.addAttribute("errorFiltro", "La fecha 'Desde' no puede ser posterior a la fecha 'Hasta'.");
            // Mantener filtros originales para que el usuario los vea en el formulario
            model.addAttribute("paramBusqueda", busqueda);
            model.addAttribute("paramFuente", fuente);
            model.addAttribute("paramFechaDesde", fechaDesde);
            model.addAttribute("paramFechaHasta", fechaHasta);
            model.addAttribute("fuentes", noticiaService.obtenerTodasLasFuentes());
            // Mostrar página sin resultados
            model.addAttribute("noticias", Page.empty());
            return "noticias/index";
        }

        // ✅ Lógica normal cuando las fechas son válidas
        noticias = noticiaService.filtrarNoticias(busqueda, fuente, fechaDesde, fechaHasta, pageable);

        model.addAttribute("noticias", noticias);
        model.addAttribute("fuentes", noticiaService.obtenerTodasLasFuentes());
        model.addAttribute("paramBusqueda", busqueda);
        model.addAttribute("paramFuente", fuente);
        model.addAttribute("paramFechaDesde", fechaDesde);
        model.addAttribute("paramFechaHasta", fechaHasta);

        return "noticias/index";
    }

    @GetMapping("/{id}")
    public String verNoticia(@PathVariable Long id, Model model) {
        model.addAttribute("noticia", noticiaService.obtenerPorId(id));
        return "noticias/show";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioNueva(Model model) {
        model.addAttribute("noticia", new Noticia());
        return "noticias/form";
    }

    @PostMapping
    public String guardarNoticia(
            @ModelAttribute Noticia noticia,
            @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
            RedirectAttributes redirectAttributes) {

        if (imagenFile != null && !imagenFile.isEmpty()) {
            try {
                String storedFileName = fileStorageService.storeFile(imagenFile, "noticias");
                noticia.setImagenUrl("/uploads/" + storedFileName);
            } catch (RuntimeException e) {
                redirectAttributes.addFlashAttribute("error", "Error al subir la imagen: " + e.getMessage());
                return "redirect:/noticias/nueva";
            }
        }
        
        noticiaService.guardar(noticia);
        redirectAttributes.addFlashAttribute("exito", "Noticia guardada correctamente");
        return "redirect:/noticias";
    }

    @PostMapping("/{id}")
    public String actualizarNoticia(
            @PathVariable Long id,
            @ModelAttribute Noticia noticia,
            @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
            RedirectAttributes redirectAttributes,
            Model model) {  // Añade Model como parámetro

        try {
            Noticia noticiaExistente = noticiaService.obtenerPorId(id);

            // Actualizar campos básicos
            noticiaExistente.setTitulo(noticia.getTitulo());
            noticiaExistente.setResumen(noticia.getResumen());
            noticiaExistente.setContenido(noticia.getContenido());
            noticiaExistente.setFuente(noticia.getFuente());
            noticiaExistente.setFechaPublicacion(noticia.getFechaPublicacion());

            if (imagenFile != null && !imagenFile.isEmpty()) {
                try {
                    String storedFileName = fileStorageService.storeFile(imagenFile, "noticias");
                    
                    // Eliminar imagen anterior si existe
                    if (noticiaExistente.getImagenUrl() != null) {
                        fileStorageService.deleteFile(noticiaExistente.getImagenUrl());
                    }
                    
                    // Actualizar URL en la entidad
                    noticiaExistente.setImagenUrl("/uploads/" + storedFileName);
                } catch (RuntimeException e) {
                    model.addAttribute("error", "Error al procesar la imagen: " + e.getMessage());
                    model.addAttribute("noticia", noticiaExistente);
                    return "noticias/form"; // Regresa a la vista con el modelo actualizado
                }
            }

            noticiaService.guardar(noticiaExistente);
            redirectAttributes.addFlashAttribute("exito", "Noticia actualizada correctamente");
            return "redirect:/noticias/" + id;
            
        } catch (RuntimeException e) {
            model.addAttribute("error", "Error al actualizar la noticia: " + e.getMessage());
            model.addAttribute("noticia", noticiaService.obtenerPorId(id));
            return "noticias/form";
        }
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("noticia", noticiaService.obtenerPorId(id));
        return "noticias/form";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarNoticia(@PathVariable Long id) {
        Noticia noticia = noticiaService.obtenerPorId(id);
        if (noticia.getImagenUrl() != null) {
            fileStorageService.deleteFile(noticia.getImagenUrl());
        }
        noticiaService.eliminar(id);
        return "redirect:/noticias";
    }
}