package com.redekrill.agendamentos.domain.model;

import jakarta.persistence.*;
import lombok.*;

/** Registro unico (id = 1) com os limites de pallets por dia e por faixa. */
@Entity
@Table(name = "configuracao_capacidade")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ConfiguracaoCapacidade {
    public static final Long ID_UNICO = 1L;

    @Id
    private Long id = ID_UNICO;

    @Column(name = "pallets_por_dia", nullable = false)
    private Integer palletsPorDia;

    @Column(name = "pallets_por_faixa", nullable = false)
    private Integer palletsPorFaixa;
}
