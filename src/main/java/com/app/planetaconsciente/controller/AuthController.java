package com.app.planetaconsciente.controller;

import com.app.planetaconsciente.model.User;
import com.app.planetaconsciente.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Collections;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    // Muestra formulario de login
    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // login.html
    }

    // Muestra dashboard (Spring Security maneja la autenticación)
    @GetMapping("/dashboard")
    public String showDashboard() {
        return "dashboard"; // dashboard.html
    }

    // Muestra formulario de registro
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register"; // register.html
    }

    // Procesa el registro de nuevos usuarios
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user) {
        user.setRoles(Collections.singletonList("USER")); // ✅ asigna rol USER por defecto
        userService.registerNewUser(user);
        return "redirect:/login?success"; // Redirige a login con mensaje de éxito
    }
}
