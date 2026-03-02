package com.duoc.monitor_gen_resumen.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UbicacionVehiculoDTO {

    private Long id;
    private String vehiculoId;
    private Double latitud;
    private Double longitud;
    private String ruta;
    private String timestamp;
}
