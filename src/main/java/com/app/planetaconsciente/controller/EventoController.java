package com.app.planetaconsciente.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize; // ✅ Usando jakarta
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.planetaconsciente.model.Evento;
import com.app.planetaconsciente.model.Reto;
import com.app.planetaconsciente.service.EventoService;
import com.app.planetaconsciente.service.RetoService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/eventos")
public class EventoController {

    private final EventoService eventoService;
    private final RetoService retoService;

    public EventoController(EventoService eventoService, RetoService retoService) {
        this.eventoService = eventoService;
        this.retoService = retoService;
    }

    // Vista principal combinada
    @GetMapping
    public String listarEventosYRetos(Model model) {
        String mesActual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        model.addAttribute("eventos", eventoService.listarTodos());
        model.addAttribute("retos", retoService.listarTodos());
        model.addAttribute("mesActual", mesActual);
        return "eventos/lista-combinada";
    }

    // --------------------- GESTIÓN DE EVENTOS ---------------------
    
    @GetMapping("/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String mostrarFormularioEventoNuevo(Model model) {
        Evento evento = new Evento();
        // Establecer fecha predeterminada: 1 hora en el futuro
        LocalDateTime ahora = LocalDateTime.now();
        evento.setFechaHora(ahora.plusHours(1));
        
        model.addAttribute("evento", evento);
        // Formatear para input datetime-local
        model.addAttribute("fechaMinima", ahora.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
        model.addAttribute("modoEdicion", false);
        return "eventos/form-evento";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardarEvento(@Valid @ModelAttribute("evento") Evento evento, 
                                BindingResult result, 
                                Model model,
                                RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            // Si hay errores, mantener la fecha mínima en el modelo
            LocalDateTime ahora = LocalDateTime.now();
            model.addAttribute("fechaMinima", ahora.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
            model.addAttribute("modoEdicion", evento.getId() != null);
            return "eventos/form-evento";
        }
        
        // Validación adicional para asegurar que la fecha no sea del pasado
        if (evento.getFechaHora().isBefore(LocalDateTime.now())) {
            result.rejectValue("fechaHora", "error.evento", "La fecha y hora no pueden ser en el pasado");
            LocalDateTime ahora = LocalDateTime.now();
            model.addAttribute("fechaMinima", ahora.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
            model.addAttribute("modoEdicion", evento.getId() != null);
            return "eventos/form-evento";
        }
        
        // Validación: no más de 2 años en el futuro
        LocalDateTime maxFecha = LocalDateTime.now().plusYears(2);
        if (evento.getFechaHora().isAfter(maxFecha)) {
            result.rejectValue("fechaHora", "error.evento", "La fecha no puede ser más de 2 años en el futuro");
            LocalDateTime ahora = LocalDateTime.now();
            model.addAttribute("fechaMinima", ahora.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
            model.addAttribute("modoEdicion", evento.getId() != null);
            return "eventos/form-evento";
        }
        
        eventoService.guardar(evento);
        redirectAttributes.addFlashAttribute("mensajeExito", 
            evento.getId() != null ? "Evento actualizado correctamente" : "Evento creado correctamente");
        return "redirect:/eventos";
    }

    @GetMapping("/{id}")
    public String verDetalleEvento(@PathVariable Long id, Model model) {
        Evento evento = eventoService.buscarPorId(id);
        if (evento == null) {
            return "redirect:/eventos";
        }
        model.addAttribute("evento", evento);
        return "eventos/detalle-evento";
    }

    @GetMapping("/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String mostrarFormularioEditarEvento(@PathVariable Long id, Model model) {
        Evento evento = eventoService.buscarPorId(id);
        if (evento == null) {
            return "redirect:/eventos";
        }
        
        LocalDateTime ahora = LocalDateTime.now();
        model.addAttribute("evento", evento);
        model.addAttribute("fechaMinima", ahora.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
        model.addAttribute("modoEdicion", true);
        return "eventos/form-evento";
    }

    @PostMapping("/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String actualizarEvento(@PathVariable Long id, 
                                   @Valid @ModelAttribute("evento") Evento evento, 
                                   BindingResult result, 
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            LocalDateTime ahora = LocalDateTime.now();
            model.addAttribute("fechaMinima", ahora.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
            model.addAttribute("modoEdicion", true);
            return "eventos/form-evento";
        }
        
        // Validación de fecha para edición
        LocalDateTime ahora = LocalDateTime.now();
        if (evento.getFechaHora().isBefore(ahora)) {
            result.rejectValue("fechaHora", "error.evento", "La fecha y hora no pueden ser en el pasado");
            model.addAttribute("fechaMinima", ahora.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
            model.addAttribute("modoEdicion", true);
            return "eventos/form-evento";
        }
        
        // Validación: no más de 2 años en el futuro
        LocalDateTime maxFecha = ahora.plusYears(2);
        if (evento.getFechaHora().isAfter(maxFecha)) {
            result.rejectValue("fechaHora", "error.evento", "La fecha no puede ser más de 2 años en el futuro");
            model.addAttribute("fechaMinima", ahora.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
            model.addAttribute("modoEdicion", true);
            return "eventos/form-evento";
        }
        
        evento.setId(id);
        eventoService.guardar(evento);
        redirectAttributes.addFlashAttribute("mensajeExito", "Evento actualizado correctamente");
        return "redirect:/eventos";
    }

    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminarEvento(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            eventoService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Evento eliminado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar el evento: " + e.getMessage());
        }
        return "redirect:/eventos";
    }

    // --------------------- GESTIÓN DE RETOS ---------------------
    
    @GetMapping("/retos/nuevo")
    @PreAuthorize("hasRole('ADMIN')")
    public String mostrarFormularioRetoNuevo(Model model) {
        model.addAttribute("reto", new Reto());
        return "eventos/form-retos";
    }

    @PostMapping("/retos/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardarReto(@Valid @ModelAttribute("reto") Reto reto, 
                              BindingResult result, 
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "eventos/form-retos";
        }
        retoService.guardar(reto);
        redirectAttributes.addFlashAttribute("mensajeExito", "Reto creado correctamente");
        return "redirect:/eventos/retos/mensuales";
    }

    @GetMapping("/retos/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String mostrarFormularioEditarReto(@PathVariable Long id, Model model) {
        Reto reto = retoService.buscarPorId(id);
        if (reto == null) {
            return "redirect:/eventos/retos/mensuales";
        }
        model.addAttribute("reto", reto);
        return "eventos/form-retos";
    }

    @PostMapping("/retos/editar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String actualizarReto(@PathVariable Long id, 
                                 @Valid @ModelAttribute("reto") Reto reto, 
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "eventos/form-retos";
        }
        reto.setId(id);
        retoService.guardar(reto);
        redirectAttributes.addFlashAttribute("mensajeExito", "Reto actualizado correctamente");
        return "redirect:/eventos/retos/mensuales";
    }

    @PostMapping("/retos/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminarReto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            retoService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensajeExito", "Reto eliminado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar el reto: " + e.getMessage());
        }
        return "redirect:/eventos/retos/mensuales";
    }

    @GetMapping("/retos/mensuales")
    public String verRetosMensuales(Model model) {
        Map<String, List<Reto>> retosPorMes = retoService.obtenerRetosAgrupadosPorMes();
        model.addAttribute("retosPorMes", retosPorMes);
        return "eventos/retos-mensuales";
    }

    @GetMapping("/retos/{id}")
    public String verDetalleReto(@PathVariable Long id, Model model) {
        Reto reto = retoService.buscarPorId(id);
        if (reto == null) {
            return "redirect:/eventos/retos/mensuales";
        }
        model.addAttribute("reto", reto);
        return "eventos/detalle-reto";
    }
}