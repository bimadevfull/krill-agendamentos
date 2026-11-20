package com.redekrill.agendamentos.presentation.dto;

import com.redekrill.agendamentos.application.dto.NovoAgendamento;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record AgendamentoRequest(
        @NotNull(message = "Data é obrigatória") LocalDate data,
        @NotNull(message = "Faixa horária é obrigatória") Long faixaHorariaId,
        @NotNull(message = "ID do pedido é obrigatório") @Positive(message = "ID do pedido inválido") Long idPedido,
        @NotBlank(message = "Fornecedor é obrigatório") @Size(max = 80, message = "Fornecedor aceita até 80 caracteres") String fornecedor,
        @NotBlank(message = "E-mail do fornecedor é obrigatório")
        @Email(message = "E-mail do fornecedor inválido")
        @Size(max = 80, message = "E-mail aceita até 80 caracteres") String emailFornecedor,
        @NotNull(message = "Tipo de caminhão é obrigatório") Long tipoCaminhaoId,
        @NotNull(message = "Tipo de paletização é obrigatório") Long tipoPaletizacaoId,
        @NotNull(message = "Quantidade de pallets é obrigatória") @Min(value = 1, message = "Quantidade de pallets deve ser ao menos 1") Integer quantidadePallets,
        @Size(max = 500, message = "Observação aceita até 500 caracteres") String observacao) {

    public NovoAgendamento toCommand() {
        return new NovoAgendamento(data, faixaHorariaId, idPedido, fornecedor, emailFornecedor,
                tipoCaminhaoId, tipoPaletizacaoId, quantidadePallets, observacao);
    }
}
