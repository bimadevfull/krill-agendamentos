package com.redekrill.agendamentos.presentation.dto;

import java.util.List;

/** Agendamento criado + avisos de capacidade excedida (o cadastro nunca e bloqueado). */
public record AgendamentoCriadoResponse(AgendamentoResponse agendamento, List<String> avisos) {
}
