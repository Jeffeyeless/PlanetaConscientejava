package com.app.planetaconsciente.config;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.app.planetaconsciente.model.User;
import com.app.planetaconsciente.repository.UserRepository;
import com.app.planetaconsciente.service.UserService;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository, 
                                     PasswordEncoder encoder,
                                     UserService userService) {
        return args -> {
            Optional<User> admin = userRepository.findByEmail("admin@admin.com");

            if (admin.isEmpty()) {
                User adminUser = new User();
                adminUser.setNombre("Administrador Principal");
                adminUser.setEmail("admin@admin.com");
                adminUser.setPassword(encoder.encode("admin123"));
                adminUser.setRoles(List.of("ADMIN"));
                adminUser.setEnabled(true); // ✅ Admin activo directamente
                
                // Establecer fecha de expiración para tokens (opcional)
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.YEAR, 1);
                adminUser.setTokenExpirationDate(cal.getTime());
                
                userRepository.save(adminUser);
                System.out.println("✅ Usuario ADMIN creado: admin@admin.com / admin123");
            }
            
            // También puedes crear el admin usando el servicio si prefieres
            userService.createAdminUser();
        };
    }
}