package com.app.planetaconsciente.repository;

import com.app.planetaconsciente.model.Calculadora;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalculadoraRepository extends JpaRepository<Calculadora, Long> {
}