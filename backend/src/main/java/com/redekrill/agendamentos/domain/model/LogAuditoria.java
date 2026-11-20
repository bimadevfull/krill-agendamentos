package com.redekrill.agendamentos.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "log_auditoria")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class LogAuditoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false, length = 80)
    private String usuario;

    @Column(nullable = false, length = 40)
    private String acao;

    @Column(nullable = false, length = 200)
    private String recurso;

    @Column(length = 500)
    private String detalhe;

    @Column(length = 60)
    private String ip;

    private Integer status;
}
