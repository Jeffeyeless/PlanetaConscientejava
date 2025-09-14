package com.app.planetaconsciente.service;

import com.app.planetaconsciente.model.User;
import com.app.planetaconsciente.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerNewUser(User user) {
        userRepository.findByEmail(user.getEmail())
            .ifPresent(u -> {
                throw new RuntimeException("El email ya está registrado");
            });

        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new RuntimeException("La contraseña no puede estar vacía");
        }

        // Asignar rol por defecto si no viene ninguno
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(List.of("USER"));
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false); // Cuenta deshabilitada hasta verificación
        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el email: " + email));
        
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            user.isEnabled(), // ✅ Ahora usa el campo enabled
            true, // accountNonExpired
            true, // credentialsNonExpired
            true, // accountNonLocked
            getAuthorities(user.getRoles())
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(List<String> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    // Métodos nuevos para verificación y recuperación

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // ✅ CORREGIDO: Devuelve Optional<User> en lugar de User
    public Optional<User> findByVerificationToken(String token) {
        return userRepository.findByVerificationToken(token);
    }

    // ✅ CORREGIDO: Devuelve Optional<User> en lugar de User
    public Optional<User> findByResetPasswordToken(String token) {
        return userRepository.findByResetPasswordToken(token);
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }

    /**
     * Calcula la fecha de expiración para tokens
     * @param hours horas de validez del token
     * @return fecha de expiración
     */
    public Date calculateExpiryDate(int hours) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR, hours);
        return cal.getTime();
    }

    /**
     * Verifica si un usuario existe y está habilitado
     */
    public boolean userExistsAndEnabled(String email) {
        return userRepository.findByEmail(email)
                .map(User::isEnabled)
                .orElse(false);
    }

    // Método para crear usuario admin (actualizado)
    public void createAdminUser() {
        if (userRepository.findByEmail("admin@planetaconsciente.com").isEmpty()) {
            User admin = new User();
            admin.setNombre("Administrador");
            admin.setEmail("admin@planetaconsciente.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRoles(List.of("ADMIN"));
            admin.setEnabled(true); // Admin activo directamente
            userRepository.save(admin);
        }
    }
}