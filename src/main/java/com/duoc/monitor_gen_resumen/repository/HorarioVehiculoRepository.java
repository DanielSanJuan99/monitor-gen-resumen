package com.duoc.monitor_gen_resumen.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.duoc.monitor_gen_resumen.entity.HorarioVehiculo;

@Repository
public interface HorarioVehiculoRepository extends JpaRepository<HorarioVehiculo, Long> {

    List<HorarioVehiculo> findByVehiculoId(String vehiculoId);

    List<HorarioVehiculo> findByFechaRegistroBetween(LocalDateTime inicio, LocalDateTime fin);

    List<HorarioVehiculo> findByVehiculoIdAndFechaRegistroBetween(
            String vehiculoId, LocalDateTime inicio, LocalDateTime fin);

    List<HorarioVehiculo> findByRuta(String ruta);
}
