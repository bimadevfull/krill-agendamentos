package com.redekrill.agendamentos.presentation.controller;

import com.redekrill.agendamentos.application.service.CatalogoService;
import com.redekrill.agendamentos.domain.model.ConfiguracaoCapacidade;
import com.redekrill.agendamentos.domain.model.FaixaHoraria;
import com.redekrill.agendamentos.domain.model.TipoCaminhao;
import com.redekrill.agendamentos.domain.model.TipoPaletizacao;
import com.redekrill.agendamentos.presentation.dto.CatalogoRequests.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Cadastros de apoio. Leitura para qualquer perfil; escrita somente ADMIN (regra no SecurityConfig). */
@RestController
@RequestMapping("/api")
@Tag(name = "Cadastros")
public class CatalogoController {

    private final CatalogoService service;

    public CatalogoController(CatalogoService service) {
        this.service = service;
    }

    // ---- tipos de caminhao ----
    @GetMapping("/tipos-caminhao")
    public List<TipoCaminhao> listarCaminhoes() {
        return service.listarCaminhoes();
    }

    @PostMapping("/tipos-caminhao")
    @ResponseStatus(HttpStatus.CREATED)
    public TipoCaminhao criarCaminhao(@Valid @RequestBody TipoCaminhaoRequest req) {
        return service.criarCaminhao(req.nome());
    }

    @PutMapping("/tipos-caminhao/{id}")
    public TipoCaminhao atualizarCaminhao(@PathVariable Long id, @Valid @RequestBody TipoCaminhaoRequest req) {
        return service.atualizarCaminhao(id, req.nome());
    }

    @DeleteMapping("/tipos-caminhao/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirCaminhao(@PathVariable Long id) {
        service.excluirCaminhao(id);
    }

    // ---- tipos de paletizacao ----
    @GetMapping("/tipos-paletizacao")
    public List<TipoPaletizacao> listarPaletizacoes() {
        return service.listarPaletizacoes();
    }

    @PostMapping("/tipos-paletizacao")
    @ResponseStatus(HttpStatus.CREATED)
    public TipoPaletizacao criarPaletizacao(@Valid @RequestBody TipoPaletizacaoRequest req) {
        return service.criarPaletizacao(req.nome(), req.fatorPallets());
    }

    @PutMapping("/tipos-paletizacao/{id}")
    public TipoPaletizacao atualizarPaletizacao(@PathVariable Long id, @Valid @RequestBody TipoPaletizacaoRequest req) {
        return service.atualizarPaletizacao(id, req.nome(), req.fatorPallets());
    }

    @DeleteMapping("/tipos-paletizacao/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirPaletizacao(@PathVariable Long id) {
        service.excluirPaletizacao(id);
    }

    // ---- faixas de horario ----
    @GetMapping("/faixas-horarias")
    public List<FaixaHoraria> listarFaixas() {
        return service.listarFaixas();
    }

    @PostMapping("/faixas-horarias")
    @ResponseStatus(HttpStatus.CREATED)
    public FaixaHoraria criarFaixa(@Valid @RequestBody FaixaHorariaRequest req) {
        return service.criarFaixa(req.descricao(), req.horaInicio(), req.horaFim());
    }

    @PutMapping("/faixas-horarias/{id}")
    public FaixaHoraria atualizarFaixa(@PathVariable Long id, @Valid @RequestBody FaixaHorariaRequest req) {
        return service.atualizarFaixa(id, req.descricao(), req.horaInicio(), req.horaFim());
    }

    @DeleteMapping("/faixas-horarias/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirFaixa(@PathVariable Long id) {
        service.excluirFaixa(id);
    }

    // ---- capacidade (controle por dia e por faixa) ----
    @GetMapping("/configuracao-capacidade")
    public ConfiguracaoCapacidade obterCapacidade() {
        return service.obterCapacidade();
    }

    @PutMapping("/configuracao-capacidade")
    public ConfiguracaoCapacidade atualizarCapacidade(@Valid @RequestBody CapacidadeRequest req) {
        return service.atualizarCapacidade(req.palletsPorDia(), req.palletsPorFaixa());
    }
}
