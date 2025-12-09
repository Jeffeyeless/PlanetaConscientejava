package com.app.planetaconsciente.config;

import com.app.planetaconsciente.model.User;
import com.app.planetaconsciente.model.UserRole;
import com.app.planetaconsciente.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@Profile("!test") // No ejecutar en pruebas
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("=== INICIALIZACIÓN DE BASE DE DATOS ===");
            System.out.println("=".repeat(50));
            
            long userCount = userRepository.count();
            System.out.println("Usuarios existentes en BD: " + userCount);
            
            // Verificar si ya existe el admin
            boolean adminExists = userRepository.findByEmail("admin@gmail.com").isPresent();
            System.out.println("¿Admin (admin@gmail.com) existe?: " + adminExists);
            
            if (!adminExists) {
                System.out.println("\n=== CREANDO USUARIO ADMINISTRADOR ===");
                
                // 1. Crear ADMIN
                User admin = new User();
                admin.setNombre("Administrador Principal");
                admin.setEmail("admin@gmail.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEnabled(true);
                
                UserRole adminRole = new UserRole(admin, "ADMIN");
                admin.setRoles(List.of(adminRole));
                
                userRepository.save(admin);
                
                System.out.println("✔ ADMIN CREADO EXITOSAMENTE");
                System.out.println("   Email: admin@gmail.com");
                System.out.println("   Contraseña: admin123");
                System.out.println("   Estado: HABILITADO");
                System.out.println("   Rol: ADMIN");
            } else {
                System.out.println("\n✓ Usuario admin ya existe en la base de datos");
            }
            
            // Verificar si existe usuario demo
            boolean userExists = userRepository.findByEmail("usuario@gmail.com").isPresent();
            System.out.println("¿Usuario demo (usuario@gmail.com) existe?: " + userExists);
            
            if (!userExists) {
                System.out.println("\n=== CREANDO USUARIO DEMO ===");
                
                // 2. Crear usuario regular
                User user = new User();
                user.setNombre("Usuario Demo");
                user.setEmail("usuario@gmail.com");
                user.setPassword(passwordEncoder.encode("usuario123"));
                user.setEnabled(true);
                
                UserRole userRole = new UserRole(user, "USER");
                user.setRoles(List.of(userRole));
                
                userRepository.save(user);
                
                System.out.println("✔ USUARIO DEMO CREADO EXITOSAMENTE");
                System.out.println("   Email: usuario@gmail.com");
                System.out.println("   Contraseña: usuario123");
                System.out.println("   Estado: HABILITADO");
                System.out.println("   Rol: USER");
            } else {
                System.out.println("\n✓ Usuario demo ya existe en la base de datos");
            }
            
            // Mostrar resumen
            System.out.println("\n" + "=".repeat(50));
            System.out.println("=== RESUMEN DE CREDENCIALES ===");
            System.out.println("=".repeat(50));
            System.out.println("ADMIN:");
            System.out.println("  Email: admin@gmail.com");
            System.out.println("  Contraseña: admin123");
            System.out.println("\nUSUARIO:");
            System.out.println("  Email: usuario@gmail.com");
            System.out.println("  Contraseña: usuario123");
            System.out.println("\n" + "=".repeat(50));
        };
    }
}