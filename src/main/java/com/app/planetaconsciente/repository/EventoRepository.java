package com.app.planetaconsciente.repository;

import com.app.planetaconsciente.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoRepository extends JpaRepository<Evento, Long> {
}