package com.app.planetaconsciente.service;

import com.app.planetaconsciente.model.Evento;
import java.util.List;

public interface EventoService {
    List<Evento> listarTodos();
    Evento guardar(Evento evento);
    Evento buscarPorId(Long id);
    void eliminar(Long id);
}