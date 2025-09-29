package com.app.planetaconsciente.repository;

import com.app.planetaconsciente.model.Calculadora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CalculadoraReporteRepository extends JpaRepository<Calculadora, Long> {
    
    // Estadísticas por clasificación
    @Query("SELECT c.clasificacion, COUNT(c) FROM Calculadora c GROUP BY c.clasificacion")
    List<Object[]> countByClasificacion();
    
    // Promedio de huella por clasificación
    @Query("SELECT c.clasificacion, AVG(c.resultado) FROM Calculadora c GROUP BY c.clasificacion")
    List<Object[]> avgHuellaByClasificacion();
    
    // Total de registros
    @Query("SELECT COUNT(c) FROM Calculadora c")
    Long getTotalRegistros();
    
    // Distribución por género
    @Query("SELECT c.sexo, COUNT(c) FROM Calculadora c GROUP BY c.sexo")
    List<Object[]> countBySexo();
    
    // Distribución por medio de transporte
    @Query("SELECT c.medioTransporte, COUNT(c) FROM Calculadora c GROUP BY c.medioTransporte")
    List<Object[]> countByMedioTransporte();
    
    // Top 5 mayores huellas - CORREGIDO: retorna List<Calculadora>
    @Query(value = "SELECT * FROM calculadoras ORDER BY resultado DESC LIMIT 5", nativeQuery = true)
    List<Calculadora> findTop5MayoresHuellas();

    // Métodos con filtros por fecha
    @Query("SELECT COUNT(c) FROM Calculadora c WHERE c.fechaCreacion BETWEEN :startDate AND :endDate")
    Long countByFechaCreacionBetween(@Param("startDate") LocalDateTime startDate, 
                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c.clasificacion, COUNT(c) FROM Calculadora c WHERE c.fechaCreacion BETWEEN :startDate AND :endDate GROUP BY c.clasificacion")
    List<Object[]> countByClasificacionAndFecha(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c.clasificacion, AVG(c.resultado) FROM Calculadora c WHERE c.fechaCreacion BETWEEN :startDate AND :endDate GROUP BY c.clasificacion")
    List<Object[]> avgHuellaByClasificacionAndFecha(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c.sexo, COUNT(c) FROM Calculadora c WHERE c.fechaCreacion BETWEEN :startDate AND :endDate GROUP BY c.sexo")
    List<Object[]> countBySexoAndFecha(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);

    // CORREGIDO: retorna List<Calculadora>
    @Query(value = "SELECT * FROM calculadoras WHERE fecha_creacion BETWEEN :startDate AND :endDate ORDER BY resultado DESC LIMIT 5", nativeQuery = true)
    List<Calculadora> findTop5MayoresHuellasByFecha(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);
}