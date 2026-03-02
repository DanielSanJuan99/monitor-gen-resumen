package com.duoc.monitor_gen_resumen.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.monitor_gen_resumen.entity.ResumenDiario;
import com.duoc.monitor_gen_resumen.repository.ResumenDiarioRepository;
import com.duoc.monitor_gen_resumen.service.ResumenService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/resumenes")
public class ResumenController {

    @Autowired
    private ResumenService resumenService;

    @Autowired
    private ResumenDiarioRepository resumenRepository;

    // ============================================================
    // ENDPOINTS PARA DOCUMENTOS JSON GENERADOS
    // ============================================================

    /**
     * GET - Lista todos los documentos JSON generados con sus URLs de acceso.
     * Ejemplo: GET /api/resumenes/documentos
     */
    @GetMapping("/documentos")
    public ResponseEntity<Map<String, Object>> listarDocumentos(HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        List<Map<String, String>> documentos = resumenService.listarDocumentosJson(baseUrl);

        Map<String, Object> respuesta = new java.util.LinkedHashMap<>();
        respuesta.put("totalDocumentos", documentos.size());
        respuesta.put("mensaje", documentos.isEmpty()
                ? "No hay documentos JSON generados aun. El scheduler genera uno cada 2 minutos."
                : "Documentos JSON disponibles. Use la URL de cada documento para ver su contenido.");
        respuesta.put("documentos", documentos);

        return ResponseEntity.ok(respuesta);
    }

    /**
     * GET - Obtiene el contenido de un documento JSON especifico.
     * Ejemplo: GET /api/resumenes/documentos/resumen_2026-03-01_143000.json
     */
    @GetMapping(value = "/documentos/{nombreArchivo}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> obtenerDocumento(@PathVariable String nombreArchivo) {
        String contenido = resumenService.obtenerDocumentoJson(nombreArchivo);
        if (contenido != null) {
            return ResponseEntity.ok(contenido);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * POST - Genera un resumen con archivo JSON manualmente para hoy.
     * Ejemplo: POST /api/resumenes/generar-json
     */
    @PostMapping("/generar-json")
    public ResponseEntity<Map<String, String>> generarResumenConJson(HttpServletRequest request) {
        resumenService.generarResumenDiarioConJson(LocalDate.now());
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

        Map<String, String> respuesta = new java.util.LinkedHashMap<>();
        respuesta.put("estado", "completado");
        respuesta.put("mensaje", "Resumen generado con archivo JSON para: " + LocalDate.now());
        respuesta.put("verDocumentos", baseUrl + "/api/resumenes/documentos");

        return ResponseEntity.ok(respuesta);
    }

    // ============================================================
    // ENDPOINTS CRUD EXISTENTES
    // ============================================================

    /**
     * GET - Obtener todos los resumenes
     */
    @GetMapping
    public ResponseEntity<List<ResumenDiario>> obtenerTodos() {
        return ResponseEntity.ok(resumenService.obtenerTodosResumenes());
    }

    /**
     * GET - Obtener resumen por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResumenDiario> obtenerPorId(@PathVariable Long id) {
        Optional<ResumenDiario> resumen = resumenRepository.findById(id);
        return resumen.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET - Obtener resumenes por fecha
     */
    @GetMapping("/fecha/{fecha}")
    public ResponseEntity<List<ResumenDiario>> obtenerPorFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(resumenService.obtenerResumenesPorFecha(fecha));
    }

    /**
     * GET - Obtener resumenes por vehiculo
     */
    @GetMapping("/vehiculo/{vehiculoId}")
    public ResponseEntity<List<ResumenDiario>> obtenerPorVehiculo(@PathVariable String vehiculoId) {
        return ResponseEntity.ok(resumenService.obtenerResumenesPorVehiculo(vehiculoId));
    }

    /**
     * POST - Generar resumen diario manualmente para una fecha
     */
    @PostMapping("/generar/{fecha}")
    public ResponseEntity<String> generarResumen(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        resumenService.generarResumenDiario(fecha);
        return ResponseEntity.ok("Resumen generado para fecha: " + fecha);
    }

    /**
     * POST - Generar resumen del dia actual
     */
    @PostMapping("/generar-hoy")
    public ResponseEntity<String> generarResumenHoy() {
        resumenService.generarResumenDiario(LocalDate.now());
        return ResponseEntity.ok("Resumen generado para hoy: " + LocalDate.now());
    }

    /**
     * PUT - Actualizar un resumen
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResumenDiario> actualizar(@PathVariable Long id,
            @RequestBody ResumenDiario resumenActualizado) {
        return resumenRepository.findById(id)
                .map(resumen -> {
                    resumen.setVehiculoId(resumenActualizado.getVehiculoId());
                    resumen.setRuta(resumenActualizado.getRuta());
                    resumen.setTotalUbicaciones(resumenActualizado.getTotalUbicaciones());
                    resumen.setTotalParadasVisitadas(resumenActualizado.getTotalParadasVisitadas());
                    resumen.setParadasVisitadas(resumenActualizado.getParadasVisitadas());
                    resumen.setHorariosRegistrados(resumenActualizado.getHorariosRegistrados());
                    resumen.setFechaResumen(resumenActualizado.getFechaResumen());
                    return ResponseEntity.ok(resumenRepository.save(resumen));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE - Eliminar un resumen
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminar(@PathVariable Long id) {
        if (resumenRepository.existsById(id)) {
            resumenRepository.deleteById(id);
            return ResponseEntity.ok("Resumen eliminado con ID: " + id);
        }
        return ResponseEntity.notFound().build();
    }
}
