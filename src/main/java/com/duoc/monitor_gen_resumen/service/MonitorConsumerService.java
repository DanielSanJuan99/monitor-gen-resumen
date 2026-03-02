package com.duoc.monitor_gen_resumen.service;

import org.springframework.kafka.support.Acknowledgment;

public interface MonitorConsumerService {

    void consumirUbicacion(String mensaje, Acknowledgment ack);

    void consumirHorario(String mensaje, Acknowledgment ack);
}
