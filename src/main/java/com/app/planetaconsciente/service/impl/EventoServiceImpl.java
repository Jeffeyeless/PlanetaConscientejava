package com.app.planetaconsciente.service.impl;

import com.app.planetaconsciente.model.Evento;
import com.app.planetaconsciente.repository.EventoRepository;
import com.app.planetaconsciente.service.EventoService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EventoServiceImpl implements EventoService {

    private final EventoRepository eventoRepository;

    public EventoServiceImpl(EventoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    @Override
    public List<Evento> listarTodos() {
        return eventoRepository.findAll();
    }

    @Override
    public Evento guardar(Evento evento) {
        return eventoRepository.save(evento);
    }

    @Override
    public Evento buscarPorId(Long id) {
        return eventoRepository.findById(id).orElse(null);
    }

    @Override
    public void eliminar(Long id) {
        eventoRepository.deleteById(id);
    }
}