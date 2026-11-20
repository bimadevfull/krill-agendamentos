package com.redekrill.agendamentos.domain.repository;

import com.redekrill.agendamentos.domain.model.TipoPaletizacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoPaletizacaoRepository extends JpaRepository<TipoPaletizacao, Long> {
    List<TipoPaletizacao> findAllByOrderByNomeAsc();
}
