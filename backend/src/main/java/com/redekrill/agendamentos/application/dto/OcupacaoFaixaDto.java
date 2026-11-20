package com.redekrill.agendamentos.application.dto;

public record OcupacaoFaixaDto(
        Long faixaId,
        String descricao,
        long usado,
        long limite,
        double percentual,
        boolean excedido) {
}
