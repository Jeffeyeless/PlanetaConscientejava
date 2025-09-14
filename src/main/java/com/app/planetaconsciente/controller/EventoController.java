package com.app.planetaconsciente.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.app.planetaconsciente.model.Evento;
import com.app.planetaconsciente.model.Reto;
import com.app.planetaconsciente.service.EventoService;
import com.app.planetaconsciente.service.RetoService;

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
        evento.setFechaHora(LocalDateTime.now());
        model.addAttribute("evento", evento);
        return "eventos/form-evento";
    }

    @PostMapping("/guardar")
    @PreAuthorize("hasRole('ADMIN')")
    public String guardarEvento(@ModelAttribute Evento evento, BindingResult result) {
        if (result.hasErrors()) {
            return "eventos/form-evento";
        }
        eventoService.guardar(evento);
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
        model.addAttribute("evento", evento);
        return "eventos/form-evento";
    }

    @PostMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminarEvento(@PathVariable Long id) {
        eventoService.eliminar(id);
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
    public String guardarReto(@ModelAttribute Reto reto, BindingResult result) {
        if (result.hasErrors()) {
            return "eventos/form-retos";
        }
        retoService.guardar(reto);
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
    public String actualizarReto(@PathVariable Long id, @ModelAttribute Reto reto, BindingResult result) {
        if (result.hasErrors()) {
            return "eventos/form-retos";
        }
        reto.setId(id);
        retoService.guardar(reto);
        return "redirect:/eventos/retos/mensuales";
    }

    @PostMapping("/retos/eliminar/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String eliminarReto(@PathVariable Long id) {
        retoService.eliminar(id);
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