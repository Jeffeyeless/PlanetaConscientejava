package com.app.planetaconsciente.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Capacitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String descripcion;
    private String urlMaterial;
    private boolean esImagen;
    private boolean esVideo;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
