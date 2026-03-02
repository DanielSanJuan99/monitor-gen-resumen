package com.duoc.monitor_gen_resumen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MonitorGenResumenApplication {

	public static void main(String[] args) {
		SpringApplication.run(MonitorGenResumenApplication.class, args);
	}
}
