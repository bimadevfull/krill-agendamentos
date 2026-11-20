package com.redekrill.agendamentos.presentation.controller;

import com.redekrill.agendamentos.application.service.AgendamentoService;
import com.redekrill.agendamentos.domain.model.StatusAgendamento;
import com.redekrill.agendamentos.presentation.dto.AgendamentoCriadoResponse;
import com.redekrill.agendamentos.presentation.dto.AgendamentoRequest;
import com.redekrill.agendamentos.presentation.dto.AgendamentoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/agendamentos")
@Tag(name = "Agendamentos")
public class AgendamentoController {

    private final AgendamentoService service;

    public AgendamentoController(AgendamentoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um agendamento (ADMIN). Excesso de capacidade gera apenas avisos.")
    public AgendamentoCriadoResponse criar(@Valid @RequestBody AgendamentoRequest req, Authentication auth) {
        var resultado = service.criar(req.toCommand(), auth.getName());
        return new AgendamentoCriadoResponse(AgendamentoResponse.from(resultado.agendamento()), resultado.avisos());
    }

    @GetMapping
    @Operation(summary = "Relatório de agendamentos filtrado por período (e status opcional)")
    public List<AgendamentoResponse> listar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) StatusAgendamento status) {
        return service.listar(inicio, fim, status).stream().map(AgendamentoResponse::from).toList();
    }

    @GetMapping("/{id}")
    public AgendamentoResponse obter(@PathVariable Long id) {
        return AgendamentoResponse.from(service.obter(id));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancela um agendamento (ADMIN)")
    public AgendamentoResponse cancelar(@PathVariable Long id) {
        return AgendamentoResponse.from(service.cancelar(id));
    }
}
