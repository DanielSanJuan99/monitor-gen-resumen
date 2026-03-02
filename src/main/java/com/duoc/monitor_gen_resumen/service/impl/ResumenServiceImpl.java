package com.duoc.monitor_gen_resumen.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.duoc.monitor_gen_resumen.entity.HorarioVehiculo;
import com.duoc.monitor_gen_resumen.entity.ResumenDiario;
import com.duoc.monitor_gen_resumen.entity.UbicacionVehiculo;
import com.duoc.monitor_gen_resumen.repository.HorarioVehiculoRepository;
import com.duoc.monitor_gen_resumen.repository.ResumenDiarioRepository;
import com.duoc.monitor_gen_resumen.repository.UbicacionVehiculoRepository;
import com.duoc.monitor_gen_resumen.service.ResumenService;

@Service
public class ResumenServiceImpl implements ResumenService {

    @Autowired
    private UbicacionVehiculoRepository ubicacionRepository;

    @Autowired
    private HorarioVehiculoRepository horarioRepository;

    @Autowired
    private ResumenDiarioRepository resumenRepository;

    @Override
    public void generarResumenDiario(LocalDate fecha) {
        System.out.println("=== Generando resumen diario para fecha: " + fecha + " ===");

        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);

        // Obtener ubicaciones del dia
        List<UbicacionVehiculo> ubicaciones = ubicacionRepository.findByFechaRegistroBetween(inicio, fin);

        // Obtener horarios del dia
        List<HorarioVehiculo> horarios = horarioRepository.findByFechaRegistroBetween(inicio, fin);

        // Agrupar ubicaciones por vehiculo
        Map<String, List<UbicacionVehiculo>> ubicacionesPorVehiculo = ubicaciones.stream()
                .collect(Collectors.groupingBy(UbicacionVehiculo::getVehiculoId));

        // Agrupar horarios por vehiculo
        Map<String, List<HorarioVehiculo>> horariosPorVehiculo = horarios.stream()
                .collect(Collectors.groupingBy(HorarioVehiculo::getVehiculoId));

        // Generar resumen por cada vehiculo
        for (Map.Entry<String, List<UbicacionVehiculo>> entry : ubicacionesPorVehiculo.entrySet()) {
            String vehiculoId = entry.getKey();
            List<UbicacionVehiculo> ubsVehiculo = entry.getValue();

            ResumenDiario resumen = new ResumenDiario();
            resumen.setVehiculoId(vehiculoId);
            resumen.setRuta(ubsVehiculo.get(0).getRuta());
            resumen.setTotalUbicaciones(ubsVehiculo.size());
            resumen.setFechaResumen(fecha);
            resumen.setFechaGeneracion(LocalDateTime.now());

            // Paradas visitadas desde los horarios
            List<HorarioVehiculo> horariosVehiculo = horariosPorVehiculo.getOrDefault(vehiculoId, List.of());

            List<String> paradasList = horariosVehiculo.stream()
                    .map(HorarioVehiculo::getParadaNombre)
                    .distinct()
                    .collect(Collectors.toList());

            resumen.setTotalParadasVisitadas(paradasList.size());
            resumen.setParadasVisitadas(String.join(", ", paradasList));

            // Registrar horarios de llegada
            String horariosStr = horariosVehiculo.stream()
                    .map(h -> h.getParadaNombre() + " (" + h.getHoraLlegada() + ")")
                    .collect(Collectors.joining("; "));
            resumen.setHorariosRegistrados(horariosStr);

            resumenRepository.save(resumen);
            System.out.println("[RESUMEN] Resumen guardado: " + resumen.toString());
        }

        System.out.println("=== Resumen diario generado exitosamente ===");
    }

    @Override
    public List<ResumenDiario> obtenerResumenesPorFecha(LocalDate fecha) {
        return resumenRepository.findByFechaResumen(fecha);
    }

    @Override
    public List<ResumenDiario> obtenerResumenesPorVehiculo(String vehiculoId) {
        return resumenRepository.findByVehiculoId(vehiculoId);
    }

    @Override
    public List<ResumenDiario> obtenerTodosResumenes() {
        return resumenRepository.findAll();
    }
}
