package com.duoc.monitor_gen_resumen.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "RESUMEN_DIARIO")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumenDiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "VEHICULO_ID", nullable = false)
    private String vehiculoId;

    @Column(name = "RUTA")
    private String ruta;

    @Column(name = "TOTAL_UBICACIONES")
    private Integer totalUbicaciones;

    @Column(name = "TOTAL_PARADAS_VISITADAS")
    private Integer totalParadasVisitadas;

    @Column(name = "PARADAS_VISITADAS", length = 2000)
    private String paradasVisitadas;

    @Column(name = "HORARIOS_REGISTRADOS", length = 2000)
    private String horariosRegistrados;

    @Column(name = "FECHA_RESUMEN", nullable = false)
    private LocalDate fechaResumen;

    @Column(name = "FECHA_GENERACION", nullable = false)
    private LocalDateTime fechaGeneracion;
}
