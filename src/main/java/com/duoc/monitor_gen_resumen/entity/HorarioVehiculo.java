package com.duoc.monitor_gen_resumen.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "HORARIOS_VEHICULOS")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HorarioVehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_horarios")
    @SequenceGenerator(name = "seq_horarios", sequenceName = "SEQ_HORARIOS_VEHICULOS", allocationSize = 1)
    private Long id;

    @Column(name = "VEHICULO_ID", nullable = false)
    private String vehiculoId;

    @Column(name = "RUTA")
    private String ruta;

    @Column(name = "PARADA_NOMBRE")
    private String paradaNombre;

    @Column(name = "HORA_LLEGADA")
    private String horaLlegada;

    @Column(name = "HORA_ESTIMADA_SALIDA")
    private String horaEstimadaSalida;

    @Column(name = "LATITUD")
    private Double latitud;

    @Column(name = "LONGITUD")
    private Double longitud;

    @Column(name = "FECHA_REGISTRO", nullable = false)
    private LocalDateTime fechaRegistro;
}
