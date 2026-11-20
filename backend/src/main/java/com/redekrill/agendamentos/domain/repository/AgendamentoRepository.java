package com.redekrill.agendamentos.domain.repository;

import com.redekrill.agendamentos.domain.model.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    @Query("""
           select coalesce(sum(a.palletsConsumidos), 0L) from Agendamento a
           where a.data = :data
             and a.status = com.redekrill.agendamentos.domain.model.StatusAgendamento.AGENDADO
           """)
    Long somarPalletsDoDia(@Param("data") LocalDate data);

    @Query("""
           select coalesce(sum(a.palletsConsumidos), 0L) from Agendamento a
           where a.data = :data
             and a.faixaHoraria.id = :faixaId
             and a.status = com.redekrill.agendamentos.domain.model.StatusAgendamento.AGENDADO
           """)
    Long somarPalletsDaFaixa(@Param("data") LocalDate data, @Param("faixaId") Long faixaId);

    /** Retorna linhas [faixaId, soma de pallets] para o dia informado. */
    @Query("""
           select a.faixaHoraria.id, coalesce(sum(a.palletsConsumidos), 0L) from Agendamento a
           where a.data = :data
             and a.status = com.redekrill.agendamentos.domain.model.StatusAgendamento.AGENDADO
           group by a.faixaHoraria.id
           """)
    List<Object[]> somarPalletsPorFaixa(@Param("data") LocalDate data);

    @Query("""
           select a from Agendamento a
           join fetch a.faixaHoraria f
           join fetch a.tipoCaminhao
           join fetch a.tipoPaletizacao
           where a.data between :inicio and :fim
           order by a.data, f.horaInicio, a.id
           """)
    List<Agendamento> buscarPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);
}
