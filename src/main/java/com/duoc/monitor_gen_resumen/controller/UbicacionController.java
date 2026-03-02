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

import com.duoc.monitor_gen_resumen.entity.UbicacionVehiculo;
import com.duoc.monitor_gen_resumen.repository.UbicacionVehiculoRepository;

@RestController
@RequestMapping("/api/ubicaciones")
public class UbicacionController {

    @Autowired
    private UbicacionVehiculoRepository ubicacionRepository;

    /**
     * GET - Obtener todas las ubicaciones registradas
     */
    @GetMapping
    public ResponseEntity<List<UbicacionVehiculo>> obtenerTodas() {
        return ResponseEntity.ok(ubicacionRepository.findAll());
    }

    /**
     * GET - Obtener ubicacion por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UbicacionVehiculo> obtenerPorId(@PathVariable Long id) {
        Optional<UbicacionVehiculo> ubicacion = ubicacionRepository.findById(id);
        return ubicacion.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET - Obtener ubicaciones por vehiculo
     */
    @GetMapping("/vehiculo/{vehiculoId}")
    public ResponseEntity<List<UbicacionVehiculo>> obtenerPorVehiculo(@PathVariable String vehiculoId) {
        return ResponseEntity.ok(ubicacionRepository.findByVehiculoId(vehiculoId));
    }

    /**
     * GET - Obtener ubicaciones por ruta
     */
    @GetMapping("/ruta/{ruta}")
    public ResponseEntity<List<UbicacionVehiculo>> obtenerPorRuta(@PathVariable String ruta) {
        return ResponseEntity.ok(ubicacionRepository.findByRuta(ruta));
    }

    /**
     * POST - Crear una ubicacion manualmente
     */
    @PostMapping
    public ResponseEntity<UbicacionVehiculo> crear(@RequestBody UbicacionVehiculo ubicacion) {
        UbicacionVehiculo guardada = ubicacionRepository.save(ubicacion);
        return ResponseEntity.ok(guardada);
    }

    /**
     * PUT - Actualizar una ubicacion
     */
    @PutMapping("/{id}")
    public ResponseEntity<UbicacionVehiculo> actualizar(@PathVariable Long id,
            @RequestBody UbicacionVehiculo ubicacionActualizada) {
        return ubicacionRepository.findById(id)
                .map(ubicacion -> {
                    ubicacion.setVehiculoId(ubicacionActualizada.getVehiculoId());
                    ubicacion.setLatitud(ubicacionActualizada.getLatitud());
                    ubicacion.setLongitud(ubicacionActualizada.getLongitud());
                    ubicacion.setRuta(ubicacionActualizada.getRuta());
                    ubicacion.setFechaRegistro(ubicacionActualizada.getFechaRegistro());
                    return ResponseEntity.ok(ubicacionRepository.save(ubicacion));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE - Eliminar una ubicacion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminar(@PathVariable Long id) {
        if (ubicacionRepository.existsById(id)) {
            ubicacionRepository.deleteById(id);
            return ResponseEntity.ok("Ubicacion eliminada con ID: " + id);
        }
        return ResponseEntity.notFound().build();
    }
}
