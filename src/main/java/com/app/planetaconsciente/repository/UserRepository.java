package com.app.planetaconsciente.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.planetaconsciente.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}