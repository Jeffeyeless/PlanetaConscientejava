package com.app.planetaconsciente.config;

import com.app.planetaconsciente.model.User;
import com.app.planetaconsciente.model.UserRole;
import com.app.planetaconsciente.repository.UserRepository;
import com.app.planetaconsciente.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public org.springframework.boot.CommandLineRunner initData(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserService userService
    ) {
        return args -> {

            if (userRepository.findByEmail("admin@admin.com").isEmpty()) {

                User admin = new User();
                admin.setNombre("Administrador Principal");
                admin.setEmail("admin@admin.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEnabled(true);

                // Crear rol ADMIN
                UserRole role = new UserRole(admin, "ADMIN");

                // Asignar lista de roles
                admin.setRoles(List.of(role));

                // Guardar admin
                userRepository.save(admin);

                System.out.println("✔ ADMIN creado exitosamente");
            }
        };
    }
}
