package com.app.planetaconsciente.service;

import com.app.planetaconsciente.model.Capacitacion;

import java.util.List;
import java.util.Optional;

public interface CapacitacionService {
    List<Capacitacion> listarTodas();
    Capacitacion guardar(Capacitacion capacitacion);
    Optional<Capacitacion> buscarPorId(Long id);
    void eliminar(Long id);
}
