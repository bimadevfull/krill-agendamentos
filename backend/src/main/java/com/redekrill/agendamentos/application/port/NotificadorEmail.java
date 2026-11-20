package com.redekrill.agendamentos.application.port;

import com.redekrill.agendamentos.domain.model.Agendamento;

/** Porta de saida para notificar o fornecedor. */
public interface NotificadorEmail {
    void enviarConfirmacao(Agendamento agendamento);
}
