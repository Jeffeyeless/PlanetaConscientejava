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
                .requestMatchers(
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/fonts/**",
                    "/webjars/**",
                    "/favicon.ico",
                    "/uploads/**"
                ).permitAll()
                
                .requestMatchers(
                    "/",
                    "/home",
                    "/inicio",
                    "/login",
                    "/register",
                    "/calculadora/**",
                    "/medio_ambiente",
                    "/eventos",
                    "/noticias",
                    "/noticias/**",
                    "/access-denied",
                    "/error"
                ).permitAll()
                
                // Rutas administrativas
                .requestMatchers(
                    "/admin/**",
                    "/noticias/nueva/**",
                    "/noticias/editar/**",
                    "/noticias/eliminar/**",
                    "/eventos/nuevo/**",
                    "/eventos/editar/**",
                    "/eventos/eliminar/**"
                ).hasRole("ADMIN")
                
                .anyRequest().authenticated()
            )
            
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/")
                .failureUrl("/login?error=true")
                .permitAll()
            )
            
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/?logout=true")
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