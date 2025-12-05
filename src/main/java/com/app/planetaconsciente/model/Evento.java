package com.app.planetaconsciente.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "eventos")
public class Evento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 5, max = 100, message = "El título debe tener entre 5 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 1000, message = "La descripción debe tener entre 10 y 1000 caracteres")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    @NotBlank(message = "La ubicación es obligatoria")
    @Size(min = 5, max = 200, message = "La ubicación debe tener entre 5 y 200 caracteres")
    @Column(nullable = false, length = 200)
    private String ubicacion;

    @NotNull(message = "La fecha y hora son obligatorias")
    @FutureOrPresent(message = "La fecha y hora no pueden ser en el pasado")
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    // Constructores
    public Evento() {
    }

    public Evento(String titulo, String descripcion, String ubicacion, LocalDateTime fechaHora) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.fechaHora = fechaHora;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    // Métodos utilitarios
    public boolean isFechaValida() {
        return fechaHora != null && !fechaHora.isBefore(LocalDateTime.now());
    }
    
    public boolean isFechaDentroDeLimite() {
        if (fechaHora == null) return false;
        LocalDateTime maxFecha = LocalDateTime.now().plusYears(2);
        return !fechaHora.isAfter(maxFecha);
    }
    
    public String getFechaHoraFormateada() {
        if (fechaHora == null) return "";
        return fechaHora.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    
    public boolean isEventoPasado() {
        return fechaHora != null && fechaHora.isBefore(LocalDateTime.now());
    }
    
    public boolean isEventoHoy() {
        if (fechaHora == null) return false;
        return fechaHora.toLocalDate().equals(LocalDateTime.now().toLocalDate());
    }
    
    public String getEstado() {
        if (fechaHora == null) return "Sin fecha";
        if (isEventoPasado()) return "Finalizado";
        if (isEventoHoy()) return "Hoy";
        return "Próximo";
    }

    @Override
    public String toString() {
        return "Evento{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", ubicacion='" + ubicacion + '\'' +
                ", fechaHora=" + fechaHora +
                '}';
    }
}