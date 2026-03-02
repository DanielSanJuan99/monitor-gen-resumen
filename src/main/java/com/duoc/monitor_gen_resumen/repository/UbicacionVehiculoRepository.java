package com.duoc.monitor_gen_resumen.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.duoc.monitor_gen_resumen.entity.UbicacionVehiculo;

@Repository
public interface UbicacionVehiculoRepository extends JpaRepository<UbicacionVehiculo, Long> {

    List<UbicacionVehiculo> findByVehiculoId(String vehiculoId);

    List<UbicacionVehiculo> findByFechaRegistroBetween(LocalDateTime inicio, LocalDateTime fin);

    List<UbicacionVehiculo> findByVehiculoIdAndFechaRegistroBetween(
            String vehiculoId, LocalDateTime inicio, LocalDateTime fin);

    List<UbicacionVehiculo> findByRuta(String ruta);
}
