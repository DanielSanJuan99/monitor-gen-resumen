package com.duoc.monitor_gen_resumen.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.duoc.monitor_gen_resumen.entity.HorarioVehiculo;
import com.duoc.monitor_gen_resumen.entity.ResumenDiario;
import com.duoc.monitor_gen_resumen.entity.UbicacionVehiculo;
import com.duoc.monitor_gen_resumen.repository.HorarioVehiculoRepository;
import com.duoc.monitor_gen_resumen.repository.ResumenDiarioRepository;
import com.duoc.monitor_gen_resumen.repository.UbicacionVehiculoRepository;
import com.duoc.monitor_gen_resumen.service.ResumenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Service
public class ResumenServiceImpl implements ResumenService {

    @Autowired
    private UbicacionVehiculoRepository ubicacionRepository;

    @Autowired
    private HorarioVehiculoRepository horarioRepository;

    @Autowired
    private ResumenDiarioRepository resumenRepository;

    @Value("${app.resumenes.json.path:resumenes-json}")
    private String jsonOutputPath;

    private final ObjectMapper objectMapper;

    public ResumenServiceImpl() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void generarResumenDiario(LocalDate fecha) {
        generarResumen(fecha, false);
    }

    @Override
    public void generarResumenDiarioConJson(LocalDate fecha) {
        generarResumen(fecha, true);
    }

    private List<ResumenDiario> generarResumen(LocalDate fecha, boolean generarJson) {
        System.out.println("=== Generando resumen para fecha: " + fecha + " ===");

        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);

        List<UbicacionVehiculo> ubicaciones = ubicacionRepository.findByFechaRegistroBetween(inicio, fin);
        List<HorarioVehiculo> horarios = horarioRepository.findByFechaRegistroBetween(inicio, fin);

        Map<String, List<UbicacionVehiculo>> ubicacionesPorVehiculo = ubicaciones.stream()
                .collect(Collectors.groupingBy(UbicacionVehiculo::getVehiculoId));

        Map<String, List<HorarioVehiculo>> horariosPorVehiculo = horarios.stream()
                .collect(Collectors.groupingBy(HorarioVehiculo::getVehiculoId));

        List<ResumenDiario> resumenesGenerados = new ArrayList<>();

        for (Map.Entry<String, List<UbicacionVehiculo>> entry : ubicacionesPorVehiculo.entrySet()) {
            String vehiculoId = entry.getKey();
            List<UbicacionVehiculo> ubsVehiculo = entry.getValue();

            ResumenDiario resumen = new ResumenDiario();
            resumen.setVehiculoId(vehiculoId);
            resumen.setRuta(ubsVehiculo.get(0).getRuta());
            resumen.setTotalUbicaciones(ubsVehiculo.size());
            resumen.setFechaResumen(fecha);
            resumen.setFechaGeneracion(LocalDateTime.now());

            List<HorarioVehiculo> horariosVehiculo = horariosPorVehiculo.getOrDefault(vehiculoId, List.of());

            List<String> paradasList = horariosVehiculo.stream()
                    .map(HorarioVehiculo::getParadaNombre)
                    .distinct()
                    .collect(Collectors.toList());

            resumen.setTotalParadasVisitadas(paradasList.size());
            resumen.setParadasVisitadas(String.join(", ", paradasList));

            String horariosStr = horariosVehiculo.stream()
                    .map(h -> h.getParadaNombre() + " (" + h.getHoraLlegada() + ")")
                    .collect(Collectors.joining("; "));
            resumen.setHorariosRegistrados(horariosStr);

            resumenRepository.save(resumen);
            resumenesGenerados.add(resumen);
            System.out.println("[RESUMEN] Resumen guardado: " + resumen.toString());
        }

        // Generar archivo JSON con todos los resumenes del periodo
        if (generarJson) {
            generarArchivoJson(fecha, resumenesGenerados, ubicaciones, horarios);
        }

        System.out.println("=== Resumen generado exitosamente ===");
        return resumenesGenerados;
    }

    private void generarArchivoJson(LocalDate fecha, List<ResumenDiario> resumenes,
            List<UbicacionVehiculo> ubicaciones, List<HorarioVehiculo> horarios) {
        try {
            // Crear directorio si no existe
            Path dirPath = Paths.get(jsonOutputPath);
            Files.createDirectories(dirPath);

            // Nombre del archivo con fecha y hora
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "resumen_" + fecha + "_" + timestamp + ".json";
            Path filePath = dirPath.resolve(fileName);

            // Construir el documento JSON completo
            Map<String, Object> documento = new LinkedHashMap<>();
            documento.put("fechaResumen", fecha.toString());
            documento.put("fechaGeneracion", LocalDateTime.now().toString());
            documento.put("totalVehiculos", resumenes.size());
            documento.put("totalUbicacionesRegistradas", ubicaciones.size());
            documento.put("totalHorariosRegistrados", horarios.size());

            // Detalle por vehiculo
            List<Map<String, Object>> detalleVehiculos = new ArrayList<>();
            for (ResumenDiario resumen : resumenes) {
                Map<String, Object> vehiculoDetalle = new LinkedHashMap<>();
                vehiculoDetalle.put("vehiculoId", resumen.getVehiculoId());
                vehiculoDetalle.put("ruta", resumen.getRuta());
                vehiculoDetalle.put("totalUbicaciones", resumen.getTotalUbicaciones());
                vehiculoDetalle.put("totalParadasVisitadas", resumen.getTotalParadasVisitadas());
                vehiculoDetalle.put("paradasVisitadas", resumen.getParadasVisitadas());
                vehiculoDetalle.put("horariosRegistrados", resumen.getHorariosRegistrados());

                // Incluir ubicaciones detalladas del vehiculo
                List<Map<String, Object>> ubicacionesDetalle = ubicaciones.stream()
                        .filter(u -> u.getVehiculoId().equals(resumen.getVehiculoId()))
                        .map(u -> {
                            Map<String, Object> ubMap = new LinkedHashMap<>();
                            ubMap.put("latitud", u.getLatitud());
                            ubMap.put("longitud", u.getLongitud());
                            ubMap.put("ruta", u.getRuta());
                            ubMap.put("fechaRegistro", u.getFechaRegistro().toString());
                            return ubMap;
                        })
                        .collect(Collectors.toList());
                vehiculoDetalle.put("ubicaciones", ubicacionesDetalle);

                // Incluir horarios detallados del vehiculo
                List<Map<String, Object>> horariosDetalle = horarios.stream()
                        .filter(h -> h.getVehiculoId().equals(resumen.getVehiculoId()))
                        .map(h -> {
                            Map<String, Object> hMap = new LinkedHashMap<>();
                            hMap.put("paradaNombre", h.getParadaNombre());
                            hMap.put("horaLlegada", h.getHoraLlegada());
                            hMap.put("horaEstimadaSalida", h.getHoraEstimadaSalida());
                            hMap.put("latitud", h.getLatitud());
                            hMap.put("longitud", h.getLongitud());
                            hMap.put("fechaRegistro", h.getFechaRegistro().toString());
                            return hMap;
                        })
                        .collect(Collectors.toList());
                vehiculoDetalle.put("horarios", horariosDetalle);

                detalleVehiculos.add(vehiculoDetalle);
            }
            documento.put("vehiculos", detalleVehiculos);

            // Escribir JSON a archivo
            objectMapper.writeValue(filePath.toFile(), documento);
            System.out.println("[JSON] Documento generado: " + filePath.toAbsolutePath());

        } catch (IOException e) {
            System.err.println("[JSON] Error al generar archivo JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<Map<String, String>> listarDocumentosJson(String baseUrl) {
        List<Map<String, String>> documentos = new ArrayList<>();
        Path dirPath = Paths.get(jsonOutputPath);

        if (!Files.exists(dirPath)) {
            return documentos;
        }

        File[] archivos = dirPath.toFile().listFiles((dir, name) -> name.endsWith(".json"));
        if (archivos == null) {
            return documentos;
        }

        for (File archivo : archivos) {
            Map<String, String> doc = new LinkedHashMap<>();
            doc.put("nombre", archivo.getName());
            doc.put("tamanio", (archivo.length() / 1024) + " KB");
            doc.put("ultimaModificacion", new java.util.Date(archivo.lastModified()).toString());
            doc.put("url", baseUrl + "/api/resumenes/documentos/" + archivo.getName());
            documentos.add(doc);
        }

        // Ordenar por nombre descendente (mas reciente primero)
        documentos.sort((a, b) -> b.get("nombre").compareTo(a.get("nombre")));
        return documentos;
    }

    @Override
    public String obtenerDocumentoJson(String nombreArchivo) {
        try {
            Path filePath = Paths.get(jsonOutputPath, nombreArchivo);
            if (Files.exists(filePath) && nombreArchivo.endsWith(".json")) {
                return Files.readString(filePath);
            }
        } catch (IOException e) {
            System.err.println("[JSON] Error al leer archivo: " + e.getMessage());
        }
        return null;
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
