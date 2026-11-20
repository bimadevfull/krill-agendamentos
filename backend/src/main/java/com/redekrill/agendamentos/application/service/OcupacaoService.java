package com.redekrill.agendamentos.application.service;

import com.redekrill.agendamentos.application.dto.OcupacaoDiaDto;
import com.redekrill.agendamentos.application.dto.OcupacaoFaixaDto;
import com.redekrill.agendamentos.domain.exception.BusinessException;
import com.redekrill.agendamentos.domain.model.ConfiguracaoCapacidade;
import com.redekrill.agendamentos.domain.repository.AgendamentoRepository;
import com.redekrill.agendamentos.domain.repository.ConfiguracaoCapacidadeRepository;
import com.redekrill.agendamentos.domain.repository.FaixaHorariaRepository;
import com.redekrill.agendamentos.domain.service.CapacidadeCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OcupacaoService {

    private final AgendamentoRepository agendamentos;
    private final FaixaHorariaRepository faixas;
    private final ConfiguracaoCapacidadeRepository configuracoes;

    public OcupacaoService(AgendamentoRepository agendamentos,
                           FaixaHorariaRepository faixas,
                           ConfiguracaoCapacidadeRepository configuracoes) {
        this.agendamentos = agendamentos;
        this.faixas = faixas;
        this.configuracoes = configuracoes;
    }

    @Transactional(readOnly = true)
    public OcupacaoDiaDto consultar(LocalDate data) {
        ConfiguracaoCapacidade cfg = configuracoes.findById(ConfiguracaoCapacidade.ID_UNICO)
                .orElseThrow(() -> new BusinessException("Configuração de capacidade não cadastrada"));

        long usadoDia = agendamentos.somarPalletsDoDia(data);

        Map<Long, Long> porFaixa = new HashMap<>();
        for (Object[] linha : agendamentos.somarPalletsPorFaixa(data)) {
            porFaixa.put(((Number) linha[0]).longValue(), ((Number) linha[1]).longValue());
        }

        long limiteFaixa = cfg.getPalletsPorFaixa();
        List<OcupacaoFaixaDto> listaFaixas = faixas.findAllByOrderByHoraInicioAsc().stream()
                .map(f -> {
                    long usado = porFaixa.getOrDefault(f.getId(), 0L);
                    return new OcupacaoFaixaDto(
                            f.getId(),
                            f.getDescricao(),
                            usado,
                            limiteFaixa,
                            CapacidadeCalculator.percentual(usado, limiteFaixa),
                            CapacidadeCalculator.excede(usado, limiteFaixa));
                })
                .toList();

        long limiteDia = cfg.getPalletsPorDia();
        return new OcupacaoDiaDto(
                data,
                usadoDia,
                limiteDia,
                CapacidadeCalculator.percentual(usadoDia, limiteDia),
                CapacidadeCalculator.excede(usadoDia, limiteDia),
                listaFaixas);
    }
}
