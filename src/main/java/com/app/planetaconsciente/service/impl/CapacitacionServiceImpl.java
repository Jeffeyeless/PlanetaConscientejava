package com.app.planetaconsciente.service.impl;

import com.app.planetaconsciente.model.Capacitacion;
import com.app.planetaconsciente.repository.CapacitacionRepository;
import com.app.planetaconsciente.service.CapacitacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CapacitacionServiceImpl implements CapacitacionService {

    @Autowired
    private CapacitacionRepository capacitacionRepository;

    @Override
    public List<Capacitacion> listarTodas() {
        return capacitacionRepository.findAll();
    }

    @Override
    public Capacitacion guardar(Capacitacion capacitacion) {
        return capacitacionRepository.save(capacitacion);
    }

    @Override
    public Optional<Capacitacion> buscarPorId(Long id) {
        return capacitacionRepository.findById(id);
    }

    @Override
    public void eliminar(Long id) {
        capacitacionRepository.deleteById(id);
    }
}