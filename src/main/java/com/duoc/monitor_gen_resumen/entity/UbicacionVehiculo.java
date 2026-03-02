package com.duoc.monitor_gen_resumen.entity;

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
@Table(name = "UBICACIONES_VEHICULOS")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UbicacionVehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "VEHICULO_ID", nullable = false)
    private String vehiculoId;

    @Column(name = "LATITUD", nullable = false)
    private Double latitud;

    @Column(name = "LONGITUD", nullable = false)
    private Double longitud;

    @Column(name = "RUTA")
    private String ruta;

    @Column(name = "FECHA_REGISTRO", nullable = false)
    private LocalDateTime fechaRegistro;
}
