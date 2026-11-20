package com.redekrill.agendamentos.application.service;

import com.redekrill.agendamentos.application.port.TokenProvider;
import com.redekrill.agendamentos.domain.exception.CredenciaisInvalidasException;
import com.redekrill.agendamentos.domain.model.Usuario;
import com.redekrill.agendamentos.domain.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    public record Sessao(String token, Usuario usuario) {
    }

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final TokenProvider tokens;
    private final AuditoriaService auditoria;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder,
                       TokenProvider tokens, AuditoriaService auditoria) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.tokens = tokens;
        this.auditoria = auditoria;
    }

    public Sessao login(String email, String senha, String ip) {
        Usuario usuario = usuarios.findByEmailIgnoreCase(email.trim()).orElse(null);
        if (usuario == null || !encoder.matches(senha, usuario.getSenha())) {
            auditoria.registrar(email, "LOGIN_FALHA", "/api/auth/login", "Credenciais inválidas", ip, 401);
            throw new CredenciaisInvalidasException();
        }
        auditoria.registrar(usuario.getEmail(), "LOGIN", "/api/auth/login", "Acesso ao sistema", ip, 200);
        return new Sessao(tokens.gerar(usuario.getEmail(), usuario.getPerfil()), usuario);
    }
}
