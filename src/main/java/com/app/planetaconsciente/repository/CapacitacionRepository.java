package com.app.planetaconsciente.repository;

import com.app.planetaconsciente.model.Capacitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CapacitacionRepository extends JpaRepository<Capacitacion, Long> {
}