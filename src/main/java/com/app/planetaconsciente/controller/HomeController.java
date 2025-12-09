package com.app.planetaconsciente.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        return "home"; // Esto debe coincidir con el nombre de tu template home.html
    }
    
    @GetMapping("/inicio")
    public String inicio(Model model) {
        return "home"; // O una página específica para usuarios autenticados
    }
}