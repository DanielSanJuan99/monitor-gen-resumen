package com.duoc.monitor_gen_resumen.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.duoc.monitor_gen_resumen.entity.ResumenDiario;

@Repository
public interface ResumenDiarioRepository extends JpaRepository<ResumenDiario, Long> {

    List<ResumenDiario> findByFechaResumen(LocalDate fecha);

    List<ResumenDiario> findByVehiculoId(String vehiculoId);

    List<ResumenDiario> findByVehiculoIdAndFechaResumen(String vehiculoId, LocalDate fecha);
}
