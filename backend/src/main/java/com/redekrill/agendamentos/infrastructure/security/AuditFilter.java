package com.redekrill.agendamentos.infrastructure.security;

import com.redekrill.agendamentos.application.service.AuditoriaService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Registra no log de auditoria cada chamada a /api (usuario, acao, recurso, status HTTP e IP).
 * Inclui tentativas negadas (401/403). Login e a propria consulta de auditoria sao tratados a parte.
 */
public class AuditFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuditFilter.class);

    private final AuditoriaService auditoria;

    public AuditFilter(AuditoriaService auditoria) {
        this.auditoria = auditoria;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            chain.doFilter(request, response);
        } finally {
            registrar(request, response);
        }
    }

    private void registrar(HttpServletRequest request, HttpServletResponse response) {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/") || path.startsWith("/api/auditoria") || path.equals("/api/auth/login")) {
            return;
        }
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean autenticado = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
            String usuario = autenticado ? auth.getName() : "anônimo";
            auditoria.registrar(usuario, acao(request.getMethod(), path), path,
                    request.getQueryString(), ip(request), response.getStatus());
        } catch (Exception e) {
            log.warn("Falha ao registrar auditoria: {}", e.getMessage());
        }
    }

    static String acao(String metodo, String path) {
        if (path.endsWith("/cancelar")) {
            return "CANCELAR";
        }
        return switch (metodo) {
            case "POST" -> "CRIAR";
            case "PUT", "PATCH" -> "ATUALIZAR";
            case "DELETE" -> "EXCLUIR";
            default -> "CONSULTAR";
        };
    }

    private static String ip(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
