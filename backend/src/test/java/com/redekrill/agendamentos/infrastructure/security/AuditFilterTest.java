package com.redekrill.agendamentos.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuditFilterTest {

    @Test
    void mapeiaMetodoHttpParaAcao() {
        assertThat(AuditFilter.acao("POST", "/api/agendamentos")).isEqualTo("CRIAR");
        assertThat(AuditFilter.acao("PUT", "/api/faixas-horarias/1")).isEqualTo("ATUALIZAR");
        assertThat(AuditFilter.acao("DELETE", "/api/tipos-caminhao/1")).isEqualTo("EXCLUIR");
        assertThat(AuditFilter.acao("GET", "/api/agendamentos")).isEqualTo("CONSULTAR");
    }

    @Test
    void identificaCancelamento() {
        assertThat(AuditFilter.acao("PATCH", "/api/agendamentos/7/cancelar")).isEqualTo("CANCELAR");
    }
}
