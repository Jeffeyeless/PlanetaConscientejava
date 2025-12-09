package com.app.planetaconsciente.service;

import com.app.planetaconsciente.model.User;
import com.app.planetaconsciente.model.UserRole;
import com.app.planetaconsciente.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("\n=== UserService.loadUserByUsername() ===");
        System.out.println("Buscando usuario con email: " + email);
        
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isEmpty()) {
            System.out.println("✗ Usuario NO encontrado en la base de datos");
            throw new UsernameNotFoundException("Usuario no encontrado con el email: " + email);
        }
        
        User user = userOptional.get();
        System.out.println("✓ Usuario encontrado:");
        System.out.println("  - Email: " + user.getEmail());
        System.out.println("  - Habilitado: " + user.isEnabled());
        System.out.println("  - Contraseña encriptada: " + (user.getPassword() != null ? "SÍ" : "NO"));
        System.out.println("  - Número de roles: " + (user.getRoles() != null ? user.getRoles().size() : 0));
        
        if (user.getRoles() != null) {
            System.out.println("  - Roles: " + user.getRoles().stream()
                    .map(UserRole::getRole)
                    .collect(Collectors.toList()));
        }
        
        // IMPORTANTE: Verificar si el usuario está habilitado
        if (!user.isEnabled()) {
            System.out.println("✗ Usuario encontrado pero NO está habilitado");
            System.out.println("  - Verification Token: " + user.getVerificationToken());
            System.out.println("  - Token Expiration: " + user.getTokenExpirationDate());
            // En desarrollo, podemos habilitarlo automáticamente
            user.setEnabled(true);
            userRepository.save(user);
            System.out.println("⚠ Usuario habilitado automáticamente (modo desarrollo)");
        }
        
        // Construir UserDetails
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(getAuthorities(user.getRoles()))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false) // Ya verificamos que está habilitado
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(List<UserRole> roles) {
        if (roles == null || roles.isEmpty()) {
            System.out.println("⚠ Usuario sin roles asignados");
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getRole()))
                .collect(Collectors.toList());
    }

    public void registerNewUser(User user) {
        System.out.println("\n=== UserService.registerNewUser() ===");
        System.out.println("Registrando usuario: " + user.getEmail());
        
        // Verificar si el email ya existe
        userRepository.findByEmail(user.getEmail())
            .ifPresent(u -> {
                System.out.println("✗ Email ya registrado: " + user.getEmail());
                throw new RuntimeException("El email ya está registrado");
            });

        // Validar contraseña
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            System.out.println("✗ Contraseña vacía");
            throw new RuntimeException("La contraseña no puede estar vacía");
        }

        // Asignar rol por defecto si no tiene
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            System.out.println("✓ Asignando rol USER por defecto");
            user.setRoles(new ArrayList<>());
            user.getRoles().add(new UserRole(user, "USER"));
        }

        // Encriptar contraseña
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        System.out.println("✓ Contraseña encriptada");
        
        // MODIFICADO: Habilitar usuario automáticamente (SIN verificación por email)
        user.setEnabled(true); // ← ¡ESTA ES LA CLAVE!
        System.out.println("✓ Usuario habilitado automáticamente (modo desarrollo sin email)");
        
        // Guardar usuario
        userRepository.save(user);
        System.out.println("✓ Usuario guardado exitosamente en la base de datos");
    }

    // Métodos de búsqueda
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByVerificationToken(String token) {
        System.out.println("Buscando usuario con token de verificación: " + token);
        Optional<User> user = userRepository.findByVerificationToken(token);
        if (user.isPresent()) {
            System.out.println("✓ Usuario encontrado para token: " + user.get().getEmail());
        } else {
            System.out.println("✗ No se encontró usuario para el token");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public Optional<User> findByResetPasswordToken(String token) {
        System.out.println("Buscando usuario con token de recuperación: " + token);
        Optional<User> user = userRepository.findByResetPasswordToken(token);
        if (user.isPresent()) {
            System.out.println("✓ Usuario encontrado para token: " + user.get().getEmail());
        } else {
            System.out.println("✗ No se encontró usuario para el token");
        }
        return user;
    }

    public void updateUser(User user) {
        System.out.println("Actualizando usuario: " + user.getEmail());
        userRepository.save(user);
        System.out.println("✓ Usuario actualizado");
    }

    /**
     * Calcula la fecha de expiración para tokens
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

    /**
     * Habilita un usuario manualmente (para pruebas o administración)
     */
    public void enableUser(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setEnabled(true);
            user.setVerificationToken(null);
            userRepository.save(user);
            System.out.println("✓ Usuario habilitado manualmente: " + email);
        } else {
            System.out.println("✗ No se pudo habilitar usuario: " + email + " (no encontrado)");
        }
    }

    /**
     * Verifica credenciales manualmente (para debugging)
     */
    public boolean verifyCredentials(String email, String rawPassword) {
        System.out.println("\n=== Verificación manual de credenciales ===");
        System.out.println("Email: " + email);
        System.out.println("Password recibido: " + rawPassword);
        
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            System.out.println("✗ Usuario no encontrado");
            return false;
        }
        
        User user = userOptional.get();
        System.out.println("Usuario encontrado:");
        System.out.println("  - Email: " + user.getEmail());
        System.out.println("  - Contraseña en BD: " + user.getPassword());
        System.out.println("  - Habilitado: " + user.isEnabled());
        
        boolean passwordMatches = passwordEncoder.matches(rawPassword, user.getPassword());
        System.out.println("¿Contraseña coincide?: " + passwordMatches);
        
        return passwordMatches;
    }

    /**
     * Lista todos los usuarios (para debugging)
     */
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Crea un usuario de prueba (solo para desarrollo)
     */
    public User createTestUser(String email, String password, String role) {
        System.out.println("\n=== Creando usuario de prueba ===");
        
        User user = new User();
        user.setNombre("Usuario Test");
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        
        List<UserRole> roles = new ArrayList<>();
        roles.add(new UserRole(user, role));
        user.setRoles(roles);
        
        User savedUser = userRepository.save(user);
        System.out.println("✓ Usuario de prueba creado:");
        System.out.println("  - Email: " + savedUser.getEmail());
        System.out.println("  - Password: " + password);
        System.out.println("  - Role: " + role);
        
        return savedUser;
    }
}