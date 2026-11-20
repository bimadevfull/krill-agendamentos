package com.redekrill.agendamentos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AgendamentosApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgendamentosApplication.class, args);
    }
}
