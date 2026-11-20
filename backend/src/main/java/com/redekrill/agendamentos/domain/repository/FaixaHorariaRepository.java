package com.redekrill.agendamentos.domain.repository;

import com.redekrill.agendamentos.domain.model.FaixaHoraria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaixaHorariaRepository extends JpaRepository<FaixaHoraria, Long> {
    List<FaixaHoraria> findAllByOrderByHoraInicioAsc();
}
