package com.redekrill.agendamentos.application.dto;

import java.time.LocalDate;

/** Comando de entrada do caso de uso "criar agendamento". */
public record NovoAgendamento(
        LocalDate data,
        Long faixaHorariaId,
        Long idPedido,
        String fornecedor,
        String emailFornecedor,
        Long tipoCaminhaoId,
        Long tipoPaletizacaoId,
        Integer quantidadePallets,
        String observacao) {
}
