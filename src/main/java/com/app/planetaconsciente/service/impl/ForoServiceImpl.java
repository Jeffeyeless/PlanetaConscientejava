package com.app.planetaconsciente.service.impl;

import com.app.planetaconsciente.model.Foro;
import com.app.planetaconsciente.repository.ForoRepository;
import com.app.planetaconsciente.service.ForoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ForoServiceImpl implements ForoService {

    @Autowired
    private ForoRepository foroRepository;

    @Override
    public List<Foro> obtenerTodos() {
        return foroRepository.findAll(); // o simplemente findAll()
    }

    @Override
    public Foro guardar(Foro foro) {
        return foroRepository.save(foro);
    }

    @Override
    public Foro obtenerPorId(Long id) {
        return foroRepository.findById(id).orElse(null);
    }

    @Override
    public void eliminar(Long id) {
        foroRepository.deleteById(id);
    }
}
