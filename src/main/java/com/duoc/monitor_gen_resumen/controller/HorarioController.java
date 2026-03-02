package com.duoc.monitor_gen_resumen.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.monitor_gen_resumen.entity.HorarioVehiculo;
import com.duoc.monitor_gen_resumen.repository.HorarioVehiculoRepository;

@RestController
@RequestMapping("/api/horarios")
public class HorarioController {

    @Autowired
    private HorarioVehiculoRepository horarioRepository;

    /**
     * GET - Obtener todos los horarios registrados
     */
    @GetMapping
    public ResponseEntity<List<HorarioVehiculo>> obtenerTodos() {
        return ResponseEntity.ok(horarioRepository.findAll());
    }

    /**
     * GET - Obtener horario por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<HorarioVehiculo> obtenerPorId(@PathVariable Long id) {
        Optional<HorarioVehiculo> horario = horarioRepository.findById(id);
        return horario.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET - Obtener horarios por vehiculo
     */
    @GetMapping("/vehiculo/{vehiculoId}")
    public ResponseEntity<List<HorarioVehiculo>> obtenerPorVehiculo(@PathVariable String vehiculoId) {
        return ResponseEntity.ok(horarioRepository.findByVehiculoId(vehiculoId));
    }

    /**
     * GET - Obtener horarios por ruta
     */
    @GetMapping("/ruta/{ruta}")
    public ResponseEntity<List<HorarioVehiculo>> obtenerPorRuta(@PathVariable String ruta) {
        return ResponseEntity.ok(horarioRepository.findByRuta(ruta));
    }

    /**
     * POST - Crear un horario manualmente
     */
    @PostMapping
    public ResponseEntity<HorarioVehiculo> crear(@RequestBody HorarioVehiculo horario) {
        HorarioVehiculo guardado = horarioRepository.save(horario);
        return ResponseEntity.ok(guardado);
    }

    /**
     * PUT - Actualizar un horario
     */
    @PutMapping("/{id}")
    public ResponseEntity<HorarioVehiculo> actualizar(@PathVariable Long id,
            @RequestBody HorarioVehiculo horarioActualizado) {
        return horarioRepository.findById(id)
                .map(horario -> {
                    horario.setVehiculoId(horarioActualizado.getVehiculoId());
                    horario.setRuta(horarioActualizado.getRuta());
                    horario.setParadaNombre(horarioActualizado.getParadaNombre());
                    horario.setHoraLlegada(horarioActualizado.getHoraLlegada());
                    horario.setHoraEstimadaSalida(horarioActualizado.getHoraEstimadaSalida());
                    horario.setLatitud(horarioActualizado.getLatitud());
                    horario.setLongitud(horarioActualizado.getLongitud());
                    horario.setFechaRegistro(horarioActualizado.getFechaRegistro());
                    return ResponseEntity.ok(horarioRepository.save(horario));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE - Eliminar un horario
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminar(@PathVariable Long id) {
        if (horarioRepository.existsById(id)) {
            horarioRepository.deleteById(id);
            return ResponseEntity.ok("Horario eliminado con ID: " + id);
        }
        return ResponseEntity.notFound().build();
    }
}
