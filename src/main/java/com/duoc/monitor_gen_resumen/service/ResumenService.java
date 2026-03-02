package com.duoc.monitor_gen_resumen.service;

import java.time.LocalDate;
import java.util.List;

import com.duoc.monitor_gen_resumen.entity.ResumenDiario;

public interface ResumenService {

    void generarResumenDiario(LocalDate fecha);

    List<ResumenDiario> obtenerResumenesPorFecha(LocalDate fecha);

    List<ResumenDiario> obtenerResumenesPorVehiculo(String vehiculoId);

    List<ResumenDiario> obtenerTodosResumenes();
}
