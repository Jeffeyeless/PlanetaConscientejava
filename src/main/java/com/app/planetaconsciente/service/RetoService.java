package com.app.planetaconsciente.service;

import java.util.List;
import java.util.Map;

import com.app.planetaconsciente.model.Reto;

public interface RetoService {
    List<Reto> listarTodos();
    Reto buscarPorId(Long id);
    void guardar(Reto reto);
    void eliminar(Long id);
    Map<String, List<Reto>> obtenerRetosAgrupadosPorMes();
}
