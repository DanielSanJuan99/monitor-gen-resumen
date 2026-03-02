package com.duoc.monitor_gen_resumen.service;

import org.springframework.kafka.support.Acknowledgment;

import com.duoc.monitor_gen_resumen.dto.HorarioDTO;
import com.duoc.monitor_gen_resumen.dto.UbicacionVehiculoDTO;

public interface MonitorConsumerService {

    void consumirUbicacion(UbicacionVehiculoDTO ubicacion, Acknowledgment ack);

    void consumirHorario(HorarioDTO horario, Acknowledgment ack);
}
