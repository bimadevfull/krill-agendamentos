package com.redekrill.agendamentos.presentation.controller;

import com.redekrill.agendamentos.domain.exception.BusinessException;
import com.redekrill.agendamentos.domain.exception.CredenciaisInvalidasException;
import com.redekrill.agendamentos.domain.exception.NotFoundException;
import com.redekrill.agendamentos.presentation.dto.ApiError;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void naoEncontradoRetorna404ComAMensagem() {
        ResponseEntity<ApiError> resposta = handler.naoEncontrado(new NotFoundException("Agendamento não encontrado"));

        assertThat(resposta.getStatusCode().value()).isEqualTo(404);
        assertThat(resposta.getBody().message()).isEqualTo("Agendamento não encontrado");
    }

    @Test
    void regraDeNegocioRetorna422() {
        ResponseEntity<ApiError> resposta = handler.negocio(new BusinessException("Data no passado"));

        assertThat(resposta.getStatusCode().value()).isEqualTo(422);
        assertThat(resposta.getBody().status()).isEqualTo(422);
        assertThat(resposta.getBody().message()).isEqualTo("Data no passado");
    }

    @Test
    void credenciaisInvalidasRetorna401() {
        ResponseEntity<ApiError> resposta = handler.credenciais(new CredenciaisInvalidasException());

        assertThat(resposta.getStatusCode().value()).isEqualTo(401);
        assertThat(resposta.getBody().message()).isEqualTo("E-mail ou senha inválidos");
    }

    @Test
    void violacaoDeIntegridadeRetorna409() {
        ResponseEntity<ApiError> resposta = handler.integridade(new DataIntegrityViolationException("duplicado"));

        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void requisicaoInvalidaRetorna400() {
        ResponseEntity<ApiError> resposta = handler.requisicaoInvalida(new IllegalArgumentException("x"));

        assertThat(resposta.getStatusCode().value()).isEqualTo(400);
        assertThat(resposta.getBody().erros()).isEmpty();
    }

    @Test
    void validacaoRetorna400ComMensagensSemRepeticao() {
        BindingResult binding = mock(BindingResult.class);
        when(binding.getFieldErrors()).thenReturn(List.of(
                new FieldError("req", "fornecedor", "Fornecedor é obrigatório"),
                new FieldError("req", "email", "Fornecedor é obrigatório"),
                new FieldError("req", "pedido", "Pedido é obrigatório")));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(binding);

        ResponseEntity<ApiError> resposta = handler.validacao(ex);

        assertThat(resposta.getStatusCode().value()).isEqualTo(400);
        assertThat(resposta.getBody().message()).isEqualTo("Dados inválidos");
        assertThat(resposta.getBody().erros())
                .containsExactly("Fornecedor é obrigatório", "Pedido é obrigatório");
    }
}
