package com.redekrill.agendamentos.domain.repository;

import com.redekrill.agendamentos.domain.model.TipoCaminhao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoCaminhaoRepository extends JpaRepository<TipoCaminhao, Long> {
    List<TipoCaminhao> findAllByOrderByNomeAsc();
}
