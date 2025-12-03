package com.app.planetaconsciente.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Entity
@Table(name = "noticias")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Noticia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 5, max = 255, message = "El título debe tener entre 5 y 255 caracteres")
    @Column(nullable = false)
    private String titulo;

    @NotBlank(message = "El resumen es obligatorio")
    @Size(min = 20, message = "El resumen debe tener al menos 20 caracteres")
    @Column(columnDefinition = "TEXT")
    private String resumen;

    @NotBlank(message = "El contenido es obligatorio")
    @Size(min = 50, message = "El contenido debe tener al menos 50 caracteres")
    @Column(columnDefinition = "TEXT")
    private String contenido;

    @Size(max = 255, message = "La fuente no puede superar los 255 caracteres")
    private String fuente;

    private String imagenUrl;

    @NotNull(message = "La fecha de publicación es obligatoria")
    private LocalDate fechaPublicacion;

    @CreatedDate
    @Column(updatable = false)
    private LocalDate fechaCreacion;
}