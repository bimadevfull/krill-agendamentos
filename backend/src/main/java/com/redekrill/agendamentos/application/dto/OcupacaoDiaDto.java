package com.redekrill.agendamentos.application.dto;

import java.time.LocalDate;
import java.util.List;

public record OcupacaoDiaDto(
        LocalDate data,
        long usado,
        long limite,
        double percentual,
        boolean excedido,
        List<OcupacaoFaixaDto> faixas) {
}
