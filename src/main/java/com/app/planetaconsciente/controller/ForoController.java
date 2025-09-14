package com.app.planetaconsciente.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.app.planetaconsciente.model.Foro;
import com.app.planetaconsciente.model.User;
import com.app.planetaconsciente.service.ForoService;
import com.app.planetaconsciente.repository.UserRepository;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/foro")
public class ForoController {

    @Autowired
    private ForoService foroService;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String listarForo(Model model, Authentication authentication) {
        model.addAttribute("publicaciones", foroService.obtenerTodos());
        if (!model.containsAttribute("publicacion")) {
            model.addAttribute("publicacion", new Foro());
        }
        
        // Agregar el usuario actual al modelo si está autenticado
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            User currentUser = userRepository.findByEmail(email)
                .orElse(null);
            model.addAttribute("currentUser", currentUser);
        }
        
        return "medioambiente/foro";
    }

    @PostMapping
    public String crearPublicacion(@Valid @ModelAttribute("publicacion") Foro foro,
                                 BindingResult result,
                                 Authentication authentication,
                                 Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("publicaciones", foroService.obtenerTodos());
            return "medioambiente/foro";
        }
        
        // Obtener el usuario actual desde la base de datos
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        foro.setUsuario(user);
        foro.setCreatedAt(LocalDateTime.now());
        foroService.guardar(foro);
        return "redirect:/foro";
    }

    @GetMapping("/edit/{id}")
    public String editarPublicacion(@PathVariable Long id, Model model) {
        Foro foro = foroService.obtenerPorId(id);
        model.addAttribute("publicacion", foro);
        return "medioambiente/foro_edit";
    }

    @PostMapping("/update/{id}")
    public String actualizarPublicacion(@PathVariable Long id,
                                      @ModelAttribute Foro foroEditado) {
        Foro foro = foroService.obtenerPorId(id);
        if (foro != null) {
            foro.setTitulo(foroEditado.getTitulo());
            foro.setContenido(foroEditado.getContenido());
            foroService.guardar(foro);
        }
        return "redirect:/foro";
    }

    @PostMapping("/delete/{id}")
    public String eliminarPublicacion(@PathVariable Long id) {
        foroService.eliminar(id);
        return "redirect:/foro";
    }
}