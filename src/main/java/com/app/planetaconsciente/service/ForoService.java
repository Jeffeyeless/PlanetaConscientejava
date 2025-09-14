package com.app.planetaconsciente.service;

import com.app.planetaconsciente.model.Foro;
import java.util.List;

public interface ForoService {
    List<Foro> obtenerTodos();
    Foro guardar(Foro foro);
    Foro obtenerPorId(Long id);
    void eliminar(Long id);
}
