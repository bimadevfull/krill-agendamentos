package com.redekrill.agendamentos.presentation.controller;

import com.redekrill.agendamentos.application.dto.OcupacaoDiaDto;
import com.redekrill.agendamentos.application.service.OcupacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/ocupacao")
@Tag(name = "Ocupação")
public class OcupacaoController {

    private final OcupacaoService service;

    public OcupacaoController(OcupacaoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Ocupação (em pallets e %) do dia e de cada faixa horária")
    public OcupacaoDiaDto consultar(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return service.consultar(data);
    }
}
