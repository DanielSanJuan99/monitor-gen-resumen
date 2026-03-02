package com.duoc.monitor_gen_resumen.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import com.duoc.monitor_gen_resumen.config.KafkaConsumerConfig;
import com.duoc.monitor_gen_resumen.dto.HorarioDTO;
import com.duoc.monitor_gen_resumen.dto.UbicacionVehiculoDTO;
import com.duoc.monitor_gen_resumen.entity.HorarioVehiculo;
import com.duoc.monitor_gen_resumen.entity.UbicacionVehiculo;
import com.duoc.monitor_gen_resumen.repository.HorarioVehiculoRepository;
import com.duoc.monitor_gen_resumen.repository.UbicacionVehiculoRepository;
import com.duoc.monitor_gen_resumen.service.MonitorConsumerService;

@Service
public class MonitorConsumerServiceImpl implements MonitorConsumerService {

    @Autowired
    private UbicacionVehiculoRepository ubicacionRepository;

    @Autowired
    private HorarioVehiculoRepository horarioRepository;

    @Override
    @KafkaListener(id = "monitorUbicacionListener",
            topics = KafkaConsumerConfig.TOPIC_UBICACIONES,
            groupId = KafkaConsumerConfig.CONSUMER_GROUP_UBICACIONES)
    public void consumirUbicacion(UbicacionVehiculoDTO ubicacionDTO, Acknowledgment ack) {
        try {
            System.out.println("[MONITOR] Ubicacion recibida: " + ubicacionDTO.toString());

            // Convertir DTO a Entity y guardar en BD
            UbicacionVehiculo ubicacion = new UbicacionVehiculo();
            ubicacion.setVehiculoId(ubicacionDTO.getVehiculoId());
            ubicacion.setLatitud(ubicacionDTO.getLatitud());
            ubicacion.setLongitud(ubicacionDTO.getLongitud());
            ubicacion.setRuta(ubicacionDTO.getRuta());
            ubicacion.setFechaRegistro(LocalDateTime.now());

            ubicacionRepository.save(ubicacion);
            System.out.println("[MONITOR] Ubicacion guardada en BD: " + ubicacion.toString());

            ack.acknowledge();

        } catch (Exception e) {
            System.out.println("[MONITOR] ERROR al procesar ubicacion: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    @KafkaListener(id = "monitorHorarioListener",
            topics = KafkaConsumerConfig.TOPIC_HORARIOS,
            groupId = KafkaConsumerConfig.CONSUMER_GROUP_HORARIOS)
    public void consumirHorario(HorarioDTO horarioDTO, Acknowledgment ack) {
        try {
            System.out.println("[MONITOR] Horario recibido: " + horarioDTO.toString());

            // Convertir DTO a Entity y guardar en BD
            HorarioVehiculo horario = new HorarioVehiculo();
            horario.setVehiculoId(horarioDTO.getVehiculoId());
            horario.setRuta(horarioDTO.getRuta());
            horario.setParadaNombre(horarioDTO.getParadaNombre());
            horario.setHoraLlegada(horarioDTO.getHoraLlegada());
            horario.setHoraEstimadaSalida(horarioDTO.getHoraEstimadaSalida());
            horario.setLatitud(horarioDTO.getLatitud());
            horario.setLongitud(horarioDTO.getLongitud());
            horario.setFechaRegistro(LocalDateTime.now());

            horarioRepository.save(horario);
            System.out.println("[MONITOR] Horario guardado en BD: " + horario.toString());

            ack.acknowledge();

        } catch (Exception e) {
            System.out.println("[MONITOR] ERROR al procesar horario: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
