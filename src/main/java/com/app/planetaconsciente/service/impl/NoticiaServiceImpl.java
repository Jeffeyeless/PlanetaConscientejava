package com.app.planetaconsciente.service.impl;

import com.app.planetaconsciente.dto.FiltroNoticia;
import com.app.planetaconsciente.model.Noticia;
import com.app.planetaconsciente.repository.NoticiaRepository;
import com.app.planetaconsciente.service.NoticiaService;
import com.app.planetaconsciente.validator.DateRangeValidator;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class NoticiaServiceImpl implements NoticiaService {

    private final NoticiaRepository noticiaRepository;

    public NoticiaServiceImpl(NoticiaRepository noticiaRepository) {
        this.noticiaRepository = noticiaRepository;
    }

    private Page<Noticia> filtrarNoticias(String busqueda, String fuente, LocalDate fechaDesde, LocalDate fechaHasta, Pageable pageable) {
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


    @Override
    public Page<Noticia> filtrar(FiltroNoticia filtro) {

        // LIMPIAR PARÁMETROS
        if (filtro.getBusqueda() != null && filtro.getBusqueda().trim().isEmpty()) {
            filtro.setBusqueda(null);
        }
        if (filtro.getFuente() != null && filtro.getFuente().trim().isEmpty()) {
            filtro.setFuente(null);
        }

        // VALIDAR FECHAS
        if (!DateRangeValidator.esRangoValido(filtro.getFechaDesde(), filtro.getFechaHasta())) {
            return Page.empty(); // El controlador mostrará el error
        }

        // PAGINACIÓN
        Pageable pageable = PageRequest.of(
            filtro.getPage(),
            filtro.getSize(),
            Sort.by("fechaPublicacion").descending()
        );

        // EJECUTAR FILTRO REAL
        return filtrarNoticias(
            filtro.getBusqueda(),
            filtro.getFuente(),
            filtro.getFechaDesde(),
            filtro.getFechaHasta(),
            pageable
        );
    }
}
