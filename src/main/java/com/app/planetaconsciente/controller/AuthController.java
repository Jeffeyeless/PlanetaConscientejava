package com.app.planetaconsciente.controller;

import com.app.planetaconsciente.model.User;
import com.app.planetaconsciente.model.UserRole;
import com.app.planetaconsciente.service.EmailService;
import com.app.planetaconsciente.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;
import java.util.List; 

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Muestra formulario de login
    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "success", required = false) String success,
                               @RequestParam(value = "verified", required = false) String verified,
                               @RequestParam(value = "resetSent", required = false) String resetSent,
                               @RequestParam(value = "passwordChanged", required = false) String passwordChanged,
                               Model model) {
        return "login";
    }

    // Muestra dashboard (Spring Security maneja la autenticación)
    @GetMapping("/dashboard")
    public String showDashboard() {
        return "dashboard";
    }

    // Muestra formulario de registro
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // Procesa el registro de nuevos usuarios - CORREGIDO
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        try {

            // Crear objeto UserRole (porque ya no usamos String)
            UserRole role = new UserRole(user, "USER");

            // Asignar rol al usuario
            user.setRoles(List.of(role));

            // Generar token de verificación
            String verificationToken = UUID.randomUUID().toString();
            user.setVerificationToken(verificationToken);
            user.setTokenExpirationDate(userService.calculateExpiryDate(24)); // 24 horas

            // Registrar usuario en la base de datos
            userService.registerNewUser(user);

            // Enviar correo de verificación
            emailService.sendConfirmationEmail(
                    user.getEmail(),
                    user.getNombre(),
                    verificationToken
            );

            return "redirect:/login?success";

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                "errorMessage",
                "Error en el registro: " + e.getMessage()
            );

            return "redirect:/register";
        }
    }

    // Confirmación de cuenta mediante token
    @GetMapping("/confirm-account")
    public String confirmAccount(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        User user = userService.findByVerificationToken(token).orElse(null);
        
        if (user != null && !user.isTokenExpired()) {
            user.setEnabled(true);
            user.setVerificationToken(null);
            userService.updateUser(user);
            
            // Usar parámetro en URL para éxito
            return "redirect:/login?verified";
        } else {
            // Para errores usar flash attribute
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Enlace inválido o expirado. Por favor regístrate nuevamente.");
            return "redirect:/register";
        }
    }

    // Muestra formulario para recuperar contraseña
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    // Procesa solicitud de recuperación de contraseña
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, 
                                      RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(email).orElse(null);
            
            if (user != null) {
                // Generar token de recuperación
                String resetToken = UUID.randomUUID().toString();
                user.setResetPasswordToken(resetToken);
                user.setTokenExpirationDate(userService.calculateExpiryDate(24)); // 24 horas
                userService.updateUser(user);
                
                // Enviar email de recuperación
                emailService.sendPasswordResetEmail(user.getEmail(), user.getNombre(), resetToken);
            }
            
            // Usar parámetro en URL para éxito
            return "redirect:/login?resetSent";
            
        } catch (Exception e) {
            // Para errores usar flash attribute
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error al procesar la solicitud: " + e.getMessage());
            return "redirect:/forgot-password";
        }
    }

    // Muestra formulario para resetear contraseña
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        User user = userService.findByResetPasswordToken(token).orElse(null);
        
        if (user != null && !user.isTokenExpired()) {
            model.addAttribute("token", token);
            return "reset-password";
        } else {
            model.addAttribute("errorMessage", "Enlace inválido o expirado.");
            return "error";
        }
    }

    // Procesa el reseteo de contraseña
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                    @RequestParam("password") String password,
                                    RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByResetPasswordToken(token).orElse(null);
            
            if (user != null && !user.isTokenExpired()) {
                // Codificar la nueva contraseña
                user.setPassword(passwordEncoder.encode(password));
                user.setResetPasswordToken(null);
                user.setTokenExpirationDate(null);
                userService.updateUser(user);
                
                // Usar parámetro en URL para éxito
                return "redirect:/login?passwordChanged";
            } else {
                // Para errores usar flash attribute
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Enlace inválido o expirado. Por favor solicita un nuevo enlace de recuperación.");
                return "redirect:/forgot-password";
            }
            
        } catch (Exception e) {
            // Para errores usar flash attribute
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error al cambiar la contraseña: " + e.getMessage());
            return "redirect:/reset-password?token=" + token;
        }
    }
}