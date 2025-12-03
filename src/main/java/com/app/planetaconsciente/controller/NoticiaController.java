package com.app.planetaconsciente.controller;

import com.app.planetaconsciente.dto.FiltroNoticia;
import com.app.planetaconsciente.model.Noticia;
import com.app.planetaconsciente.service.FileStorageService;
import com.app.planetaconsciente.service.NoticiaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;


@Controller
@RequestMapping("/noticias")
@RequiredArgsConstructor
public class NoticiaController {

    private final NoticiaService noticiaService;
    private final FileStorageService fileStorageService;

    @GetMapping
    public String listarNoticias(
            @ModelAttribute("filtro") FiltroNoticia filtro,
            @RequestParam(required = false, name = "generar_pdf") String generarPdf,
            Model model) {

                // Normalizar paginación
                if (filtro.getPage() < 0) {
                    filtro.setPage(0);
                }
                if (filtro.getSize() <= 0) {
                    filtro.setSize(9);
                }

                // ❗ Generar PDF
                if ("1".equals(generarPdf)) {
                    String url = UriComponentsBuilder.fromPath("/exportar/noticias/pdf")
                            .queryParamIfPresent("busqueda", Optional.ofNullable(filtro.getBusqueda()))
                            .queryParamIfPresent("fuente", Optional.ofNullable(filtro.getFuente()))
                            .queryParamIfPresent("fecha_desde", Optional.ofNullable(filtro.getFechaDesde()))
                            .queryParamIfPresent("fecha_hasta", Optional.ofNullable(filtro.getFechaHasta()))
                            .build()
                            .toUriString();

                    return "redirect:" + url;
                }

                // ❗ El service se encarga de validar fechas y limpiar filtros
                Page<Noticia> noticias = noticiaService.filtrar(filtro);

                // Si las fechas no son válidas, el service ya devolvió Page.empty()
                if (!noticias.hasContent() &&
                    filtro.getFechaDesde() != null &&
                    filtro.getFechaHasta() != null &&
                    filtro.getFechaDesde().isAfter(filtro.getFechaHasta())) {

                    model.addAttribute("errorFiltro", "La fecha 'Desde' no puede ser posterior a la fecha 'Hasta'.");
                }

                // Datos para la vista
                model.addAttribute("noticias", noticias);
                model.addAttribute("fuentes", noticiaService.obtenerTodasLasFuentes());
                model.addAttribute("filtro", filtro);

                return "noticias/index";
            }

    @GetMapping("/{id}")
    public String verNoticia(@PathVariable Long id, Model model) {
        model.addAttribute("noticia", noticiaService.obtenerPorId(id));
        return "noticias/show";
    }

    @GetMapping("/nueva")
    public String mostrarFormularioNueva(Model model) {
        model.addAttribute("noticiaRequest", new Noticia());
        model.addAttribute("id", null);
        model.addAttribute("imagenActual", null);
        return "noticias/form";
    }

    @PostMapping
    public String guardarNoticia(
            @Valid @ModelAttribute("noticiaRequest") Noticia noticia,
            BindingResult result,
            @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Si hay errores de validación → volver al formulario
        if (result.hasErrors()) {
            model.addAttribute("noticiaRequest", noticia);
            model.addAttribute("id", null);
            model.addAttribute("imagenActual", null);
            return "noticias/form";
        }

        // Subida de imagen (si aplica)
        if (imagenFile != null && !imagenFile.isEmpty()) {
            try {
                String storedFileName = fileStorageService.storeFile(imagenFile, "noticias");
                noticia.setImagenUrl("/uploads/" + storedFileName);
            } catch (RuntimeException e) {
                model.addAttribute("error", "Error al subir la imagen: " + e.getMessage());
                return "noticias/form";
            }
        }

        noticiaService.guardar(noticia);
        redirectAttributes.addFlashAttribute("exito", "Noticia guardada correctamente");
        return "redirect:/noticias";
    }

    @PostMapping("/{id}")
    public String actualizarNoticia(
            @PathVariable Long id,
            @Valid @ModelAttribute("noticiaRequest") Noticia noticia,
            BindingResult result,
            @RequestParam(value = "imagenFile", required = false) MultipartFile imagenFile,
            RedirectAttributes redirectAttributes,
            Model model) {

        Noticia noticiaExistente = noticiaService.obtenerPorId(id);

        // Si hay errores de validación → regresar al form con los datos existentes
        if (result.hasErrors()) {
            model.addAttribute("noticiaRequest", noticia);
            model.addAttribute("id", id);
            model.addAttribute("imagenActual", noticiaExistente.getImagenUrl());
            return "noticias/form";
        }

        // Actualizar campos básicos
        noticiaExistente.setTitulo(noticia.getTitulo());
        noticiaExistente.setResumen(noticia.getResumen());
        noticiaExistente.setContenido(noticia.getContenido());
        noticiaExistente.setFuente(noticia.getFuente());
        noticiaExistente.setFechaPublicacion(noticia.getFechaPublicacion());

        // Imagen nueva
        if (imagenFile != null && !imagenFile.isEmpty()) {
            try {
                String storedFileName = fileStorageService.storeFile(imagenFile, "noticias");

                if (noticiaExistente.getImagenUrl() != null) {
                    fileStorageService.deleteFile(noticiaExistente.getImagenUrl());
                }

                noticiaExistente.setImagenUrl("/uploads/" + storedFileName);

            } catch (RuntimeException e) {
                model.addAttribute("error", "Error al procesar la imagen: " + e.getMessage());
                model.addAttribute("noticiaRequest", noticiaExistente);
                model.addAttribute("id", id);
                model.addAttribute("imagenActual", noticiaExistente.getImagenUrl());
                return "noticias/form";
            }
        }

        if (result.hasErrors()) {
            noticia.setImagenUrl(noticiaExistente.getImagenUrl()); // mantener imagen actual
            model.addAttribute("noticiaRequest", noticia);
            return "noticias/form";
        }

        noticiaService.guardar(noticiaExistente);
        redirectAttributes.addFlashAttribute("exito", "Noticia actualizada correctamente");

        return "redirect:/noticias/" + id;
    }

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Noticia noticia = noticiaService.obtenerPorId(id);

        model.addAttribute("noticiaRequest", noticia);
        model.addAttribute("id", id);
        model.addAttribute("imagenActual", noticia.getImagenUrl());

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