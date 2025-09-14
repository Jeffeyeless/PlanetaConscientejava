package com.app.planetaconsciente.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private TemplateEngine templateEngine;

    /**
     * Envía email de confirmación de registro
     */
    public void sendConfirmationEmail(String toEmail, String nombre, String verificationToken) {
        String subject = "Confirma tu registro - Planeta Consciente";
        String confirmationUrl = "http://localhost:8070/confirm-account?token=" + verificationToken;
        
        // Usando Thymeleaf template
        Context context = new Context();
        context.setVariable("nombre", nombre);
        context.setVariable("confirmationUrl", confirmationUrl);
        
        String htmlContent = templateEngine.process("email/confirmation", context);
        
        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    /**
     * Envía email de recuperación de contraseña
     */
    public void sendPasswordResetEmail(String toEmail, String nombre, String resetToken) {
        String subject = "Recuperación de contraseña - Planeta Consciente";
        String resetUrl = "http://localhost:8070/reset-password?token=" + resetToken;
        
        Context context = new Context();
        context.setVariable("nombre", nombre);
        context.setVariable("resetUrl", resetUrl);
        
        String htmlContent = templateEngine.process("email/password-reset", context);
        
        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    /**
     * Método para enviar emails HTML
     */
    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el email", e);
        }
    }

    /**
     * Método simple para emails de texto plano (opcional)
     */
    public void sendSimpleEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        
        mailSender.send(message);
    }
}