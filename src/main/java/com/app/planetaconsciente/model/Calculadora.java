package com.app.planetaconsciente.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "calculadoras")
@Data
public class Calculadora {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Min(1) @Max(120)
    @Column(nullable = false)
    private Integer edad;
    
    @NotBlank
    @Pattern(regexp = "^(M|F|O|P)$")
    @Column(length = 1, nullable = false)
    private String sexo;
    
    @Column(name = "medio_transporte", nullable = false)
    private Integer medioTransporte;
    
    @Column(name = "tipo_combustible", nullable = false)
    private Integer tipoCombustible;
    
    @DecimalMin("0.0")
    @Column(name = "km_automovil_dia")
    private double kmAutomovilDia = 0.0;
    
    @DecimalMin("0.0")
    @Column(name = "km_bicicleta_dia")
    private double kmBicicletaDia = 0.0;
    
    @Column(name = "frecuencia_transporte_publico", nullable = false)
    private Integer frecuenciaTransportePublico;
    
    @Min(0)
    @Column(name = "vuelos_cortos_anuales", nullable = false)
    private Integer vuelosCortosAnuales = 0;
    
    @Min(0)
    @Column(name = "vuelos_largos_anuales", nullable = false)
    private Integer vuelosLargosAnuales = 0;
    
    @DecimalMin("0.0")
    @Column(name = "consumo_electricidad")
    private double consumoElectricidad = 0.0;
    
    @Column(name = "tipo_energia", nullable = false)
    private Integer tipoEnergia;
    
    @Min(0)
    @Column(name = "bolsas_basura", nullable = false)
    private Integer bolsasBasura = 0;
    
    @DecimalMin("0.0") @DecimalMax("100.0")
    @Column(name = "porcentaje_reciclaje")
    private double porcentajeReciclaje = 0.0;
    
    @DecimalMin("0.0")
    @Column(name = "consumo_agua")
    private double consumoAgua = 0.0;
    
    @Column(name = "tipo_dieta", nullable = false)
    private Integer tipoDieta;
    
    @Column(nullable = false)
    private double resultado;
    
    @NotBlank
    @Column(nullable = false, length = 50)
    private String clasificacion;
    
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}