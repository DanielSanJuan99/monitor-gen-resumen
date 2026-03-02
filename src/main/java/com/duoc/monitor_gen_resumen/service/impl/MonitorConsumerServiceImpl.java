package com.duoc.monitor_gen_resumen.service.impl;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import com.duoc.monitor_gen_resumen.config.KafkaConsumerConfig;
import com.duoc.monitor_gen_resumen.entity.HorarioVehiculo;
import com.duoc.monitor_gen_resumen.entity.UbicacionVehiculo;
import com.duoc.monitor_gen_resumen.repository.HorarioVehiculoRepository;
import com.duoc.monitor_gen_resumen.repository.UbicacionVehiculoRepository;
import com.duoc.monitor_gen_resumen.service.MonitorConsumerService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MonitorConsumerServiceImpl implements MonitorConsumerService {

    @Autowired
    private UbicacionVehiculoRepository ubicacionRepository;

    @Autowired
    private HorarioVehiculoRepository horarioRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @KafkaListener(id = "monitorUbicacionListener",
            topics = KafkaConsumerConfig.TOPIC_UBICACIONES,
            groupId = KafkaConsumerConfig.CONSUMER_GROUP_UBICACIONES)
    public void consumirUbicacion(String mensaje, Acknowledgment ack) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> datos = objectMapper.readValue(mensaje, Map.class);

            System.out.println("[MONITOR] Ubicacion recibida: " + datos);

            UbicacionVehiculo ubicacion = new UbicacionVehiculo();
            ubicacion.setVehiculoId(String.valueOf(datos.get("vehiculoId")));
            ubicacion.setLatitud(((Number) datos.get("latitud")).doubleValue());
            ubicacion.setLongitud(((Number) datos.get("longitud")).doubleValue());
            ubicacion.setRuta(String.valueOf(datos.get("ruta")));
            ubicacion.setFechaRegistro(LocalDateTime.now());

            ubicacionRepository.save(ubicacion);
            System.out.println("[MONITOR] Ubicacion guardada en BD: vehiculo=" + ubicacion.getVehiculoId());

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
    public void consumirHorario(String mensaje, Acknowledgment ack) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> datos = objectMapper.readValue(mensaje, Map.class);

            System.out.println("[MONITOR] Horario recibido: " + datos);

            HorarioVehiculo horario = new HorarioVehiculo();
            horario.setVehiculoId(String.valueOf(datos.get("vehiculoId")));
            horario.setRuta(String.valueOf(datos.get("ruta")));
            horario.setParadaNombre(String.valueOf(datos.get("paradaNombre")));
            horario.setHoraLlegada(String.valueOf(datos.get("horaLlegada")));
            horario.setHoraEstimadaSalida(String.valueOf(datos.get("horaEstimadaSalida")));
            horario.setLatitud(datos.get("latitud") != null ? ((Number) datos.get("latitud")).doubleValue() : null);
            horario.setLongitud(datos.get("longitud") != null ? ((Number) datos.get("longitud")).doubleValue() : null);
            horario.setFechaRegistro(LocalDateTime.now());

            horarioRepository.save(horario);
            System.out.println("[MONITOR] Horario guardado en BD: vehiculo=" + horario.getVehiculoId());

            ack.acknowledge();

        } catch (Exception e) {
            System.out.println("[MONITOR] ERROR al procesar horario: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
