package com.app.planetaconsciente.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NoticiaRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 5, max = 255, message = "El título debe tener entre 5 y 255 caracteres")
    private String titulo;

    @NotBlank(message = "El resumen es obligatorio")
    @Size(min = 20, message = "El resumen debe tener al menos 20 caracteres")
    private String resumen;

    @NotBlank(message = "El contenido es obligatorio")
    @Size(min = 50, message = "El contenido debe tener al menos 50 caracteres")
    private String contenido;

    @Size(max = 255, message = "La fuente no puede superar los 255 caracteres")
    private String fuente;

    @NotNull(message = "La fecha de publicación es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaPublicacion;
}
