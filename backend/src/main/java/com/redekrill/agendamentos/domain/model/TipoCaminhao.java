package com.redekrill.agendamentos.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipo_caminhao")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TipoCaminhao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String nome;
}
