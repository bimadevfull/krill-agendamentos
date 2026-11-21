package com.redekrill.agendamentos.application.service;

import com.redekrill.agendamentos.application.port.TokenProvider;
import com.redekrill.agendamentos.domain.exception.CredenciaisInvalidasException;
import com.redekrill.agendamentos.domain.model.Perfil;
import com.redekrill.agendamentos.domain.model.Usuario;
import com.redekrill.agendamentos.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock UsuarioRepository usuarios;
    @Mock PasswordEncoder encoder;
    @Mock TokenProvider tokens;
    @Mock AuditoriaService auditoria;

    @InjectMocks AuthService service;

    private Usuario admin() {
        return new Usuario(1L, "Administrador", "admin@krill.com", "hash", Perfil.ADMIN);
    }

    @Test
    void loginValidoRetornaTokenERegistraAuditoria() {
        when(usuarios.findByEmailIgnoreCase("admin@krill.com")).thenReturn(Optional.of(admin()));
        when(encoder.matches("admin123", "hash")).thenReturn(true);
        when(tokens.gerar("admin@krill.com", Perfil.ADMIN)).thenReturn("token-jwt");

        AuthService.Sessao sessao = service.login("admin@krill.com", "admin123", "127.0.0.1");

        assertThat(sessao.token()).isEqualTo("token-jwt");
        assertThat(sessao.usuario().getPerfil()).isEqualTo(Perfil.ADMIN);
        verify(auditoria).registrar(eq("admin@krill.com"), eq("LOGIN"), anyString(), anyString(),
                eq("127.0.0.1"), eq(200));
    }

    @Test
    void loginIgnoraEspacosNoEmail() {
        when(usuarios.findByEmailIgnoreCase("admin@krill.com")).thenReturn(Optional.of(admin()));
        when(encoder.matches("admin123", "hash")).thenReturn(true);
        when(tokens.gerar(any(), any())).thenReturn("t");

        AuthService.Sessao sessao = service.login("  admin@krill.com  ", "admin123", "ip");

        assertThat(sessao.token()).isEqualTo("t");
    }

    @Test
    void senhaIncorretaLancaExcecaoERegistraFalha() {
        when(usuarios.findByEmailIgnoreCase("admin@krill.com")).thenReturn(Optional.of(admin()));
        when(encoder.matches("errada", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.login("admin@krill.com", "errada", "10.0.0.1"))
                .isInstanceOf(CredenciaisInvalidasException.class);

        verify(auditoria).registrar(eq("admin@krill.com"), eq("LOGIN_FALHA"), anyString(), anyString(),
                eq("10.0.0.1"), eq(401));
        verify(tokens, never()).gerar(any(), any());
    }

    @Test
    void usuarioInexistenteLancaExcecaoSemGerarToken() {
        when(usuarios.findByEmailIgnoreCase("ninguem@krill.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login("ninguem@krill.com", "qualquer", "ip"))
                .isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessage("E-mail ou senha inválidos");

        verify(tokens, never()).gerar(any(), any());
    }
}
