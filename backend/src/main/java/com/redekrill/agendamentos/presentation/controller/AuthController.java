package com.redekrill.agendamentos.presentation.controller;

import com.redekrill.agendamentos.application.service.AuthService;
import com.redekrill.agendamentos.presentation.dto.LoginRequest;
import com.redekrill.agendamentos.presentation.dto.LoginResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @SecurityRequirements
    public LoginResponse login(@Valid @RequestBody LoginRequest req, HttpServletRequest http) {
        String forwarded = http.getHeader("X-Forwarded-For");
        String ip = (forwarded != null && !forwarded.isBlank()) ? forwarded.split(",")[0].trim() : http.getRemoteAddr();
        var sessao = authService.login(req.email(), req.senha(), ip);
        return new LoginResponse(sessao.token(), sessao.usuario().getNome(),
                sessao.usuario().getEmail(), sessao.usuario().getPerfil().name());
    }
}
