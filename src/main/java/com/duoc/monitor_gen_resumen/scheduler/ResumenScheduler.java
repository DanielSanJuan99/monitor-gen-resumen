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
     * Genera el resumen cada 2 minutos.
     * Resume las ubicaciones y horarios del dia, generando un archivo JSON.
     */
    @Scheduled(cron = "0 */2 * * * *")
    public void generarResumenPeriodico() {
        System.out.println("=== [SCHEDULER] Generando resumen periodico (cada 2 minutos) ===");
        resumenService.generarResumenDiarioConJson(LocalDate.now());
        System.out.println("=== [SCHEDULER] Resumen periodico completado ===");
    }
}
