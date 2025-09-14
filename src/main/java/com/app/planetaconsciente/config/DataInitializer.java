package com.app.planetaconsciente.config;

import java.util.Collections;
import java.util.Optional;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.app.planetaconsciente.model.User;
import com.app.planetaconsciente.repository.UserRepository;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            Optional<User> admin = userRepository.findByEmail("admin@admin.com");

            if (admin.isEmpty()) {
                User adminUser = new User();
                adminUser.setNombre("Administrador Principal");
                adminUser.setEmail("admin@admin.com");
                adminUser.setPassword(encoder.encode("admin123"));
                adminUser.setRoles(Collections.singletonList("ADMIN"));
                userRepository.save(adminUser);
                System.out.println("✅ Usuario ADMIN creado: admin@admin.com / admin123");
            }
        };
    }
}
