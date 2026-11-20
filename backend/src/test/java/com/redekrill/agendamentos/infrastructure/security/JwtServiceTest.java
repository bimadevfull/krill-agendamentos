package com.redekrill.agendamentos.infrastructure.security;

import com.redekrill.agendamentos.domain.model.Perfil;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-caracteres!";

    @Test
    void geraEValidaToken() {
        JwtService jwt = new JwtService(SEGREDO, 60);

        String token = jwt.gerar("admin@krill.com", Perfil.ADMIN);
        var claims = jwt.validar(token);

        assertThat(claims).isPresent();
        assertThat(claims.get().getSubject()).isEqualTo("admin@krill.com");
        assertThat(claims.get().get("perfil", String.class)).isEqualTo("ADMIN");
    }

    @Test
    void rejeitaTokenInvalido() {
        JwtService jwt = new JwtService(SEGREDO, 60);
        assertThat(jwt.validar("nao-e-um-token")).isEmpty();
    }

    @Test
    void rejeitaTokenAssinadoComOutroSegredo() {
        JwtService emissor = new JwtService("outro-segredo-com-mais-de-32-caracteres-xyz", 60);
        JwtService validador = new JwtService(SEGREDO, 60);
        assertThat(validador.validar(emissor.gerar("a@a.com", Perfil.COMUM))).isEmpty();
    }

    @Test
    void rejeitaTokenExpirado() {
        JwtService jwt = new JwtService(SEGREDO, -1);
        assertThat(jwt.validar(jwt.gerar("a@a.com", Perfil.COMUM))).isEmpty();
    }

    @Test
    void exigeSegredoComTamanhoMinimo() {
        assertThatThrownBy(() -> new JwtService("curto", 60)).isInstanceOf(IllegalStateException.class);
    }
}
