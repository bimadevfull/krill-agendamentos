package com.redekrill.agendamentos.domain.repository;

import com.redekrill.agendamentos.domain.model.ConfiguracaoCapacidade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfiguracaoCapacidadeRepository extends JpaRepository<ConfiguracaoCapacidade, Long> {
}
