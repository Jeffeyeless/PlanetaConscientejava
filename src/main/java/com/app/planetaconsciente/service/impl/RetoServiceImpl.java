package com.app.planetaconsciente.service.impl;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.app.planetaconsciente.model.Reto;
import com.app.planetaconsciente.repository.RetoRepository;
import com.app.planetaconsciente.service.RetoService;

@Service
public class RetoServiceImpl implements RetoService {

    private final RetoRepository retoRepository;

    public RetoServiceImpl(RetoRepository retoRepository) {
        this.retoRepository = retoRepository;
    }

    @Override
    public List<Reto> listarTodos() {
        return retoRepository.findAll();
    }

    @Override
    public Reto buscarPorId(Long id) {
        return retoRepository.findById(id).orElse(null);
    }

    @Override
    public void guardar(Reto reto) {
        retoRepository.save(reto);
    }

    @Override
    public void eliminar(Long id) {
        retoRepository.deleteById(id);
    }

    @Override
    public Map<String, List<Reto>> obtenerRetosAgrupadosPorMes() {
        List<Reto> retos = retoRepository.findAll();
        return retos.stream()
            .collect(Collectors.groupingBy(Reto::getMes, TreeMap::new, Collectors.toList()));
    }
}
