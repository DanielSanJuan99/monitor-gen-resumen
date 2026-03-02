package com.duoc.monitor_gen_resumen.scheduler;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.duoc.monitor_gen_resumen.service.ResumenService;

@Component
public class ResumenScheduler {

    @Autowired
    private ResumenService resumenService;

    /**
     * Genera el resumen diario todos los dias a las 23:55.
     * Resume las ubicaciones y horarios del dia, mostrando
     * los lugares visitados y horarios de llegada.
     */
    @Scheduled(cron = "0 55 23 * * *")
    public void generarResumenAlFinalDelDia() {
        System.out.println("=== [SCHEDULER] Generando resumen diario automatico ===");
        resumenService.generarResumenDiario(LocalDate.now());
        System.out.println("=== [SCHEDULER] Resumen diario completado ===");
    }
}
