package com.app.planetaconsciente.controller;

import com.app.planetaconsciente.model.User;
import com.app.planetaconsciente.service.EmailService;
import com.app.planetaconsciente.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    // Muestra formulario de login
    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "success", required = false) String success,
                               @RequestParam(value = "verified", required = false) String verified,
                               @RequestParam(value = "resetSent", required = false) String resetSent,
                               @RequestParam(value = "passwordChanged", required = false) String passwordChanged,
                               Model model) {
        if (success != null) {
            model.addAttribute("message", "¡Registro completado! Por favor revisa tu email para confirmar tu cuenta.");
        }
        if (verified != null) {
            model.addAttribute("message", "¡Cuenta verificada exitosamente! Ya puedes iniciar sesión.");
        }
        if (resetSent != null) {
            model.addAttribute("message", "Se ha enviado un enlace de recuperación a tu email.");
        }
        if (passwordChanged != null) {
            model.addAttribute("message", "Contraseña cambiada exitosamente. Ya puedes iniciar sesión.");
        }
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

    // Procesa el registro de nuevos usuarios
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        try {
            user.setRoles(Collections.singletonList("USER"));
            
            // Generar token de verificación
            String verificationToken = UUID.randomUUID().toString();
            user.setVerificationToken(verificationToken);
            user.setTokenExpirationDate(userService.calculateExpiryDate(24)); // 24 horas de validez
            
            // Registrar usuario
            userService.registerNewUser(user);
            
            // Enviar email de confirmación
            emailService.sendConfirmationEmail(user.getEmail(), user.getNombre(), verificationToken);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "¡Registro completado! Se ha enviado un email de verificación a " + user.getEmail() + 
                ". Por favor revisa tu bandeja de entrada y haz clic en el enlace para activar tu cuenta.");
            
            return "redirect:/login?success";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error en el registro: " + e.getMessage());
            return "redirect:/register";
        }
    }

    // Confirmación de cuenta mediante token
    @GetMapping("/confirm-account")
    public String confirmAccount(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        // ✅ CORREGIDO: Usar .orElse(null)
        User user = userService.findByVerificationToken(token).orElse(null);
        
        if (user != null && !user.isTokenExpired()) {
            user.setEnabled(true);
            user.setVerificationToken(null);
            userService.updateUser(user);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "¡Cuenta verificada exitosamente! Ya puedes iniciar sesión.");
            return "redirect:/login?verified";
        } else {
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
            // ✅ CORREGIDO: Usar .orElse(null)
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
            
            // Por seguridad, siempre mostrar el mismo mensaje aunque el email no exista
            redirectAttributes.addFlashAttribute("successMessage", 
                "Si el email existe en nuestro sistema, recibirás un enlace de recuperación.");
            return "redirect:/login?resetSent";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error al procesar la solicitud: " + e.getMessage());
            return "redirect:/forgot-password";
        }
    }

    // Muestra formulario para resetear contraseña
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        // ✅ CORREGIDO: Usar .orElse(null)
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
            // ✅ CORREGIDO: Usar .orElse(null)
            User user = userService.findByResetPasswordToken(token).orElse(null);
            
            if (user != null && !user.isTokenExpired()) {
                // Codificar la nueva contraseña
                user.setPassword(passwordEncoder.encode(password));
                user.setResetPasswordToken(null);
                user.setTokenExpirationDate(null);
                userService.updateUser(user);
                
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Contraseña cambiada exitosamente. Ya puedes iniciar sesión.");
                return "redirect:/login?passwordChanged";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Enlace inválido o expirado. Por favor solicita un nuevo enlace de recuperación.");
                return "redirect:/forgot-password";
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error al cambiar la contraseña: " + e.getMessage());
            return "redirect:/reset-password?token=" + token;
        }
    }
    @Autowired
    private PasswordEncoder passwordEncoder;
}