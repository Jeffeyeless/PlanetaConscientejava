package com.app.planetaconsciente.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import lombok.Data;

@Data
public class FiltroNoticia {

    private String busqueda;

    private String fuente;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaDesde;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaHasta;

    private int page = 0;

    private int size = 9;
}
