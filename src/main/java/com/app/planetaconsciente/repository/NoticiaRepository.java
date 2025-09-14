package com.app.planetaconsciente.repository;

import com.app.planetaconsciente.model.Noticia;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface NoticiaRepository extends JpaRepository<Noticia, Long> {

    @Query("SELECT n FROM Noticia n WHERE " +
           "(:busqueda IS NULL OR LOWER(n.titulo) LIKE LOWER(CONCAT('%', :busqueda, '%'))) AND " +
           "(:fuente IS NULL OR n.fuente = :fuente) AND " +
           "(:fechaDesde IS NULL OR n.fechaPublicacion >= :fechaDesde) AND " +
           "(:fechaHasta IS NULL OR n.fechaPublicacion <= :fechaHasta)")
    Page<Noticia> filtrarNoticias(
            @Param("busqueda") String busqueda,
            @Param("fuente") String fuente,
            @Param("fechaDesde") LocalDate fechaDesde,
            @Param("fechaHasta") LocalDate fechaHasta,
            Pageable pageable);
    @Query("SELECT DISTINCT n.fuente FROM Noticia n WHERE n.fuente IS NOT NULL")
    List<String> findDistinctFuentes();
}