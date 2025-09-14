package com.app.planetaconsciente.service;

import com.app.planetaconsciente.model.Noticia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface NoticiaService {
    Page<Noticia> filtrarNoticias(String busqueda, String fuente, LocalDate fechaDesde, LocalDate fechaHasta, Pageable pageable);
    List<String> obtenerTodasLasFuentes();
    Noticia obtenerPorId(Long id);
    Noticia guardar(Noticia noticia);
    void eliminar(Long id);
}