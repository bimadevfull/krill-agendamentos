package com.redekrill.agendamentos.presentation.dto;

import jakarta.validation.constraints.*;

import java.time.LocalTime;

public final class CatalogoRequests {
    private CatalogoRequests() {
    }

    public record TipoCaminhaoRequest(
            @NotBlank(message = "Nome é obrigatório") @Size(max = 50, message = "Nome aceita até 50 caracteres") String nome) {
    }

    public record TipoPaletizacaoRequest(
            @NotBlank(message = "Nome é obrigatório") @Size(max = 50, message = "Nome aceita até 50 caracteres") String nome,
            @NotNull(message = "Fator é obrigatório") @Min(value = 1, message = "Fator deve ser ao menos 1") Integer fatorPallets) {
    }

    public record FaixaHorariaRequest(
            @NotBlank(message = "Descrição é obrigatória") @Size(max = 50, message = "Descrição aceita até 50 caracteres") String descricao,
            @NotNull(message = "Horário inicial é obrigatório") LocalTime horaInicio,
            @NotNull(message = "Horário final é obrigatório") LocalTime horaFim) {
    }

    public record CapacidadeRequest(
            @NotNull(message = "Limite por dia é obrigatório") @Min(value = 1, message = "Limite por dia deve ser ao menos 1") Integer palletsPorDia,
            @NotNull(message = "Limite por faixa é obrigatório") @Min(value = 1, message = "Limite por faixa deve ser ao menos 1") Integer palletsPorFaixa) {
    }
}
