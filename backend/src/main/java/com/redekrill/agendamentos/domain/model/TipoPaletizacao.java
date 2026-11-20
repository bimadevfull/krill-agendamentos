package com.redekrill.agendamentos.domain.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Tipo de paletizacao. O fator multiplica a quantidade de pallets ocupada na faixa:
 * Paletizado = 1, Nao Paletizado = 2 (dobro).
 */
@Entity
@Table(name = "tipo_paletizacao")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TipoPaletizacao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nome;

    @Column(name = "fator_pallets", nullable = false)
    private Integer fatorPallets;
}
