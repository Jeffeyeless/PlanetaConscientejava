package com.app.planetaconsciente.service.impl;

import com.app.planetaconsciente.model.Noticia;
import com.app.planetaconsciente.repository.NoticiaRepository;
import com.app.planetaconsciente.service.NoticiaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class NoticiaServiceImpl implements NoticiaService {

    private final NoticiaRepository noticiaRepository;

    public NoticiaServiceImpl(NoticiaRepository noticiaRepository) {
        this.noticiaRepository = noticiaRepository;
    }

    @Override
    public Page<Noticia> filtrarNoticias(String busqueda, String fuente, LocalDate fechaDesde, LocalDate fechaHasta, Pageable pageable) {
        return noticiaRepository.filtrarNoticias(busqueda, fuente, fechaDesde, fechaHasta, pageable);
    }

    @Override
    public List<String> obtenerTodasLasFuentes() {
        return noticiaRepository.findDistinctFuentes();
    }

    @Override
    public Noticia obtenerPorId(Long id) {
        return noticiaRepository.findById(id).orElseThrow();
    }

    @Override
    public Noticia guardar(Noticia noticia) {
        return noticiaRepository.save(noticia);
    }

    @Override
    public void eliminar(Long id) {
        noticiaRepository.deleteById(id);
    }
}