package com.app.planetaconsciente.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .headers(headers -> headers
                .frameOptions().sameOrigin()
            )
            
            .authorizeHttpRequests(auth -> auth
                // 1. RECURSOS ESTÁTICOS (acceso total)
                .requestMatchers(
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/fonts/**",
                    "/webjars/**",
                    "/favicon.ico",
                    "/uploads/**"
                ).permitAll()

                
                // 2. RUTAS PÚBLICAS MÍNIMAS (sin registro necesario)
                .requestMatchers(
                    "/login",               // Formulario de login
                    "/register",            // Formulario de registro
                    "/confirm-account",     // Confirmación de email
                    "/forgot-password",     // Recuperación de contraseña
                    "/reset-password",      // Reset de contraseña
                    "/reset-password/**",   // Reset de contraseña con token
                    "/access-denied",       // Página de acceso denegado
                    "/error"                // Página de error
                ).permitAll()
                
                // 3. RUTAS ADMINISTRATIVAS (solo para ADMIN)
                .requestMatchers(
                    "/admin/**",
                    "/noticias/nueva/**",
                    "/noticias/editar/**",
                    "/noticias/eliminar/**",
                    "/eventos/nuevo/**",
                    "/eventos/editar/**",
                    "/eventos/eliminar/**"
                ).hasRole("ADMIN")
                
                // 4. TODAS LAS DEMÁS RUTAS requieren usuario AUTENTICADO y VERIFICADO
                .requestMatchers(
                    "/",                // Home principal
                    "/inicio",              // Página de inicio
                    "/calculadora/**",      // Calculadora ecológica
                    "/medio_ambiente",      // Información medio ambiente
                    "/eventos",             // Lista de eventos
                    "/noticias",            // Lista de noticias
                    "/noticias/**",         // Detalles de noticias
                    "/perfil",              // Perfil de usuario
                    "/mi-cuenta",           // Configuración de cuenta
                    "/comunidad",           // Comunidad de usuarios
                    "/recursos",            // Recursos educativos
                    "/proyectos"            // Proyectos comunitarios
                ).authenticated()           // Solo usuarios autenticados
                
                // 5. Cualquier otra ruta no listada también requiere autenticación
                .anyRequest().authenticated()
            )
            
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)  // Redirige al home después del login
                .failureUrl("/login?error=true")
                .permitAll()
            )
            
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/?logout=true")  // Al logout, va a la página pública
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .permitAll()
            )
            
            .exceptionHandling(handling -> handling
                .accessDeniedPage("/access-denied")
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}