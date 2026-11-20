package com.redekrill.agendamentos.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "agendamento")
@Getter @Setter @NoArgsConstructor
public class Agendamento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data;

    @ManyToOne(optional = false)
    @JoinColumn(name = "faixa_horaria_id")
    private FaixaHoraria faixaHoraria;

    @Column(name = "id_pedido", nullable = false)
    private Long idPedido;

    @Column(nullable = false, length = 80)
    private String fornecedor;

    @Column(name = "email_fornecedor", nullable = false, length = 80)
    private String emailFornecedor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tipo_caminhao_id")
    private TipoCaminhao tipoCaminhao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tipo_paletizacao_id")
    private TipoPaletizacao tipoPaletizacao;

    /** Quantidade informada no pedido. */
    @Column(name = "quantidade_pallets", nullable = false)
    private Integer quantidadePallets;

    /** Pallets efetivamente ocupados (quantidade x fator da paletizacao). Congelado na criacao. */
    @Column(name = "pallets_consumidos", nullable = false)
    private Integer palletsConsumidos;

    @Column(length = 500)
    private String observacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAgendamento status = StatusAgendamento.AGENDADO;

    @Column(name = "criado_por", nullable = false, length = 80)
    private String criadoPor;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;
}
