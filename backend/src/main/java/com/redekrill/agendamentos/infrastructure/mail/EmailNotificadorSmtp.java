package com.redekrill.agendamentos.infrastructure.mail;

import com.redekrill.agendamentos.application.port.NotificadorEmail;
import com.redekrill.agendamentos.domain.model.Agendamento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

/** Envia o e-mail de confirmacao ao fornecedor. Roda de forma assincrona e nunca derruba o cadastro. */
@Component
public class EmailNotificadorSmtp implements NotificadorEmail {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificadorSmtp.class);
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final JavaMailSender mailSender;
    private final String remetente;

    public EmailNotificadorSmtp(JavaMailSender mailSender, @Value("${app.mail.from}") String remetente) {
        this.mailSender = mailSender;
        this.remetente = remetente;
    }

    @Async
    @Override
    public void enviarConfirmacao(Agendamento ag) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(remetente);
            msg.setTo(ag.getEmailFornecedor());
            msg.setSubject("Agendamento de recebimento confirmado - Pedido " + ag.getIdPedido());
            msg.setText(montarCorpo(ag));
            mailSender.send(msg);
            log.info("E-mail de confirmação enviado para {}", ag.getEmailFornecedor());
        } catch (Exception e) {
            log.warn("Não foi possível enviar o e-mail para {}: {}", ag.getEmailFornecedor(), e.getMessage());
        }
    }

    static String montarCorpo(Agendamento ag) {
        StringBuilder sb = new StringBuilder();
        sb.append("Olá, ").append(ag.getFornecedor()).append("!\n\n");
        sb.append("Seu agendamento de recebimento na Rede Krill foi registrado com as informações abaixo.\n\n");
        sb.append("Pedido: ").append(ag.getIdPedido()).append('\n');
        sb.append("Data: ").append(ag.getData().format(DATA)).append('\n');
        sb.append("Faixa horária: ").append(ag.getFaixaHoraria().getDescricao()).append('\n');
        sb.append("Tipo de caminhão: ").append(ag.getTipoCaminhao().getNome()).append('\n');
        sb.append("Paletização: ").append(ag.getTipoPaletizacao().getNome()).append('\n');
        sb.append("Quantidade de pallets: ").append(ag.getQuantidadePallets()).append('\n');
        if (ag.getObservacao() != null) {
            sb.append("Observação: ").append(ag.getObservacao()).append('\n');
        }
        sb.append("\nEm caso de dúvidas, responda a este e-mail.\n\nRede Krill");
        return sb.toString();
    }
}
