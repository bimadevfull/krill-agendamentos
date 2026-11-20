package com.redekrill.agendamentos.application.port;

import com.redekrill.agendamentos.domain.model.Perfil;

/** Porta de saida para emissao de tokens de acesso. */
public interface TokenProvider {
    String gerar(String email, Perfil perfil);
}
