package com.app.planetaconsciente.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            
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
                    "/uploads/**",
                    "/h2-console/**"
                ).permitAll()

                // 2. RUTAS PÚBLICAS (sin autenticación) - ASEGÚRATE DE INCLUIR "/"
                .requestMatchers(
                    "/",                // Home público - ¡ESTO ES CLAVE!
                    "/login",           // Formulario de login
                    "/register",        // Formulario de registro
                    "/confirm-account", // Confirmación de email
                    "/forgot-password", // Recuperación de contraseña
                    "/reset-password",  // Reset de contraseña
                    "/reset-password/**", // Reset de contraseña con token
                    "/access-denied",   // Página de acceso denegado
                    "/error",           // Página de error
                    "/debug-auth",      // Ruta de prueba
                    "/debug/**"         // Todas las rutas de debug
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
                
                // 4. RUTAS QUE REQUIEREN AUTENTICACIÓN (cualquier rol)
                .requestMatchers(
                    "/inicio",              // Página de inicio personal
                    "/calculadora/**",      // Calculadora ecológica
                    "/medio_ambiente",      // Información medio ambiente
                    "/eventos",             // Lista de eventos
                    "/noticias",            // Lista de noticias
                    "/noticias/**",         // Detalles de noticias
                    "/perfil",              // Perfil de usuario
                    "/mi-cuenta",           // Configuración de cuenta
                    "/comunidad",           // Comunidad de usuarios
                    "/recursos",            // Recursos educativos
                    "/proyectos",           // Proyectos comunitarios
                    "/dashboard"            // Dashboard de usuario
                ).authenticated()
                
                // 5. Cualquier otra ruta requiere autenticación
                .anyRequest().authenticated()
            )
            
            .formLogin(form -> form
                .loginPage("/login")              // Página de login personalizada
                .loginProcessingUrl("/login")     // URL donde Spring procesa el login
                .defaultSuccessUrl("/inicio", true) // Redirige aquí después del login exitoso
                .failureUrl("/login?error=true")  // Redirige aquí si falla el login
                .usernameParameter("username")    // Nombre del parámetro para el username (email)
                .passwordParameter("password")    // Nombre del parámetro para la contraseña
                .permitAll()
            )
            
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?logout=true")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            
            .exceptionHandling(handling -> handling
                .accessDeniedPage("/access-denied")
            );

        return http.build();
    }
    
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
            "/h2-console/**",
            "/error/**"
        );
    }
    
    // Opcional: Para evitar redirecciones automáticas después del login
    @Bean
    public AuthenticationSuccessHandler successHandler() {
        SimpleUrlAuthenticationSuccessHandler handler = new SimpleUrlAuthenticationSuccessHandler();
        handler.setUseReferer(false); // Evita usar la página anterior como referencia
        return handler;
    }
}