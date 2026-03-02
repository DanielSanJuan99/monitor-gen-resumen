package com.duoc.monitor_gen_resumen.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.duoc.monitor_gen_resumen.entity.ResumenDiario;

public interface ResumenService {

    void generarResumenDiario(LocalDate fecha);

    /**
     * Genera el resumen diario y lo persiste como archivo JSON en disco.
     */
    void generarResumenDiarioConJson(LocalDate fecha);

    List<ResumenDiario> obtenerResumenesPorFecha(LocalDate fecha);

    List<ResumenDiario> obtenerResumenesPorVehiculo(String vehiculoId);

    List<ResumenDiario> obtenerTodosResumenes();

    /**
     * Lista todos los documentos JSON generados con su nombre y URL de acceso.
     */
    List<Map<String, String>> listarDocumentosJson(String baseUrl);

    /**
     * Obtiene el contenido de un documento JSON por nombre de archivo.
     */
    String obtenerDocumentoJson(String nombreArchivo);
}
