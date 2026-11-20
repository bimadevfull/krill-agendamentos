package com.redekrill.agendamentos.presentation.dto;

import com.redekrill.agendamentos.domain.model.Agendamento;
import com.redekrill.agendamentos.domain.model.StatusAgendamento;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AgendamentoResponse(
        Long id,
        LocalDate data,
        Long faixaHorariaId,
        String faixaHoraria,
        Long idPedido,
        String fornecedor,
        String emailFornecedor,
        Long tipoCaminhaoId,
        String tipoCaminhao,
        Long tipoPaletizacaoId,
        String tipoPaletizacao,
        Integer quantidadePallets,
        Integer palletsConsumidos,
        String observacao,
        StatusAgendamento status,
        String criadoPor,
        LocalDateTime criadoEm) {

    public static AgendamentoResponse from(Agendamento a) {
        return new AgendamentoResponse(
                a.getId(), a.getData(),
                a.getFaixaHoraria().getId(), a.getFaixaHoraria().getDescricao(),
                a.getIdPedido(), a.getFornecedor(), a.getEmailFornecedor(),
                a.getTipoCaminhao().getId(), a.getTipoCaminhao().getNome(),
                a.getTipoPaletizacao().getId(), a.getTipoPaletizacao().getNome(),
                a.getQuantidadePallets(), a.getPalletsConsumidos(), a.getObservacao(),
                a.getStatus(), a.getCriadoPor(), a.getCriadoEm());
    }
}
