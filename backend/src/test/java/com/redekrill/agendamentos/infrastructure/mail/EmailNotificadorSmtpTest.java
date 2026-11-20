package com.redekrill.agendamentos.infrastructure.mail;

import com.redekrill.agendamentos.domain.model.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailNotificadorSmtpTest {

    private Agendamento agendamento() {
        Agendamento a = new Agendamento();
        a.setId(1L);
        a.setData(LocalDate.of(2026, 11, 20));
        a.setFaixaHoraria(new FaixaHoraria(1L, "08:00 - 10:00", LocalTime.of(8, 0), LocalTime.of(10, 0)));
        a.setIdPedido(4521L);
        a.setFornecedor("Fornecedor Exemplo");
        a.setEmailFornecedor("contato@exemplo.com");
        a.setTipoCaminhao(new TipoCaminhao(1L, "Carreta"));
        a.setTipoPaletizacao(new TipoPaletizacao(1L, "Paletizado", 1));
        a.setQuantidadePallets(30);
        a.setPalletsConsumidos(30);
        return a;
    }

    @Test
    void corpoDoEmailTrazAsInformacoesDoAgendamento() {
        String corpo = EmailNotificadorSmtp.montarCorpo(agendamento());

        assertThat(corpo).contains("Fornecedor Exemplo", "4521", "20/11/2026", "08:00 - 10:00",
                "Carreta", "Paletizado", "30");
        assertThat(corpo).doesNotContain("Observação");
    }

    @Test
    void corpoDoEmailIncluiObservacaoQuandoExiste() {
        Agendamento a = agendamento();
        a.setObservacao("Descarga lateral");
        assertThat(EmailNotificadorSmtp.montarCorpo(a)).contains("Observação: Descarga lateral");
    }

    @Test
    void enviaEmailParaOFornecedor() {
        JavaMailSender sender = mock(JavaMailSender.class);
        new EmailNotificadorSmtp(sender, "no-reply@krill.com").enviarConfirmacao(agendamento());

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(sender).send(captor.capture());
        assertThat(captor.getValue().getTo()).containsExactly("contato@exemplo.com");
        assertThat(captor.getValue().getSubject()).contains("4521");
    }

    @Test
    void falhaNoEnvioNaoPropagaExcecao() {
        JavaMailSender sender = mock(JavaMailSender.class);
        doThrow(new MailSendException("smtp fora do ar")).when(sender).send(any(SimpleMailMessage.class));

        assertThatCode(() -> new EmailNotificadorSmtp(sender, "no-reply@krill.com")
                .enviarConfirmacao(agendamento())).doesNotThrowAnyException();
    }
}
