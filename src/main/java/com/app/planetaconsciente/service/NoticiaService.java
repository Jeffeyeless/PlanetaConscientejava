package com.app.planetaconsciente.service;

import com.app.planetaconsciente.model.Noticia;
import com.app.planetaconsciente.dto.FiltroNoticia;
import org.springframework.data.domain.Page;

import java.util.List;

public interface NoticiaService {
    Page<Noticia> filtrar(FiltroNoticia filtro);

    List<String> obtenerTodasLasFuentes();
    Noticia obtenerPorId(Long id);
    Noticia guardar(Noticia noticia);
    void eliminar(Long id);
}