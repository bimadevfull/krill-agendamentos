package com.redekrill.agendamentos.application.service;

import com.redekrill.agendamentos.application.dto.NovoAgendamento;
import com.redekrill.agendamentos.application.port.NotificadorEmail;
import com.redekrill.agendamentos.domain.exception.BusinessException;
import com.redekrill.agendamentos.domain.exception.NotFoundException;
import com.redekrill.agendamentos.domain.model.*;
import com.redekrill.agendamentos.domain.repository.*;
import com.redekrill.agendamentos.domain.service.CapacidadeCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class AgendamentoService {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private final AgendamentoRepository agendamentos;
    private final FaixaHorariaRepository faixas;
    private final TipoCaminhaoRepository caminhoes;
    private final TipoPaletizacaoRepository paletizacoes;
    private final ConfiguracaoCapacidadeRepository configuracoes;
    private final NotificadorEmail notificador;

    public AgendamentoService(AgendamentoRepository agendamentos,
                              FaixaHorariaRepository faixas,
                              TipoCaminhaoRepository caminhoes,
                              TipoPaletizacaoRepository paletizacoes,
                              ConfiguracaoCapacidadeRepository configuracoes,
                              NotificadorEmail notificador) {
        this.agendamentos = agendamentos;
        this.faixas = faixas;
        this.caminhoes = caminhoes;
        this.paletizacoes = paletizacoes;
        this.configuracoes = configuracoes;
        this.notificador = notificador;
    }

    /** Resultado da criacao: o agendamento salvo e os avisos de capacidade (nunca bloqueiam). */
    public record Resultado(Agendamento agendamento, List<String> avisos) {
    }

    @Transactional
    public Resultado criar(NovoAgendamento cmd, String usuario) {
        FaixaHoraria faixa = faixas.findById(cmd.faixaHorariaId())
                .orElseThrow(() -> new NotFoundException("Faixa horária não encontrada"));
        TipoCaminhao caminhao = caminhoes.findById(cmd.tipoCaminhaoId())
                .orElseThrow(() -> new NotFoundException("Tipo de caminhão não encontrado"));
        TipoPaletizacao paletizacao = paletizacoes.findById(cmd.tipoPaletizacaoId())
                .orElseThrow(() -> new NotFoundException("Tipo de paletização não encontrado"));
        ConfiguracaoCapacidade cfg = configuracoes.findById(ConfiguracaoCapacidade.ID_UNICO)
                .orElseThrow(() -> new BusinessException("Configuração de capacidade não cadastrada"));

        int consumo = CapacidadeCalculator.palletsConsumidos(cmd.quantidadePallets(), paletizacao.getFatorPallets());
        long totalDia = agendamentos.somarPalletsDoDia(cmd.data()) + consumo;
        long totalFaixa = agendamentos.somarPalletsDaFaixa(cmd.data(), faixa.getId()) + consumo;

        List<String> avisos = new ArrayList<>();
        if (CapacidadeCalculator.excede(totalDia, cfg.getPalletsPorDia())) {
            avisos.add(String.format(PT_BR,
                    "Capacidade diária excedida: %d de %d pallets (%.1f%%).",
                    totalDia, cfg.getPalletsPorDia(),
                    CapacidadeCalculator.percentual(totalDia, cfg.getPalletsPorDia())));
        }
        if (CapacidadeCalculator.excede(totalFaixa, cfg.getPalletsPorFaixa())) {
            avisos.add(String.format(PT_BR,
                    "Capacidade da faixa %s excedida: %d de %d pallets (%.1f%%).",
                    faixa.getDescricao(), totalFaixa, cfg.getPalletsPorFaixa(),
                    CapacidadeCalculator.percentual(totalFaixa, cfg.getPalletsPorFaixa())));
        }

        Agendamento ag = new Agendamento();
        ag.setData(cmd.data());
        ag.setFaixaHoraria(faixa);
        ag.setIdPedido(cmd.idPedido());
        ag.setFornecedor(cmd.fornecedor().trim());
        ag.setEmailFornecedor(cmd.emailFornecedor().trim());
        ag.setTipoCaminhao(caminhao);
        ag.setTipoPaletizacao(paletizacao);
        ag.setQuantidadePallets(cmd.quantidadePallets());
        ag.setPalletsConsumidos(consumo);
        ag.setObservacao(cmd.observacao() == null || cmd.observacao().isBlank() ? null : cmd.observacao().trim());
        ag.setStatus(StatusAgendamento.AGENDADO);
        ag.setCriadoPor(usuario);
        ag.setCriadoEm(LocalDateTime.now());

        Agendamento salvo = agendamentos.save(ag);
        notificador.enviarConfirmacao(salvo);
        return new Resultado(salvo, avisos);
    }

    @Transactional
    public Agendamento cancelar(Long id) {
        Agendamento ag = obter(id);
        if (ag.getStatus() == StatusAgendamento.CANCELADO) {
            throw new BusinessException("O agendamento já está cancelado");
        }
        ag.setStatus(StatusAgendamento.CANCELADO);
        return agendamentos.save(ag);
    }

    @Transactional(readOnly = true)
    public Agendamento obter(Long id) {
        return agendamentos.findById(id)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Agendamento> listar(LocalDate inicio, LocalDate fim, StatusAgendamento status) {
        if (inicio == null || fim == null) {
            throw new BusinessException("Informe o período (início e fim)");
        }
        if (fim.isBefore(inicio)) {
            throw new BusinessException("A data final não pode ser anterior à data inicial");
        }
        List<Agendamento> lista = agendamentos.buscarPorPeriodo(inicio, fim);
        if (status == null) {
            return lista;
        }
        return lista.stream().filter(a -> a.getStatus() == status).toList();
    }
}
