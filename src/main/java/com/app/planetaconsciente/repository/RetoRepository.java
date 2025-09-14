package com.app.planetaconsciente.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.planetaconsciente.model.Reto;

public interface RetoRepository extends JpaRepository<Reto, Long> {
    List<Reto> findByMesIgnoreCase(String mes);
}
