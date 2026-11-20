package com.redekrill.agendamentos.application.service;

import com.redekrill.agendamentos.domain.exception.BusinessException;
import com.redekrill.agendamentos.domain.model.ConfiguracaoCapacidade;
import com.redekrill.agendamentos.domain.model.FaixaHoraria;
import com.redekrill.agendamentos.domain.repository.AgendamentoRepository;
import com.redekrill.agendamentos.domain.repository.ConfiguracaoCapacidadeRepository;
import com.redekrill.agendamentos.domain.repository.FaixaHorariaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OcupacaoServiceTest {

    private static final LocalDate DATA = LocalDate.of(2026, 11, 20);

    @Mock AgendamentoRepository agendamentos;
    @Mock FaixaHorariaRepository faixas;
    @Mock ConfiguracaoCapacidadeRepository configuracoes;

    @InjectMocks OcupacaoService service;

    @Test
    void calculaOcupacaoDoDiaEDeCadaFaixa() {
        when(configuracoes.findById(ConfiguracaoCapacidade.ID_UNICO))
                .thenReturn(Optional.of(new ConfiguracaoCapacidade(1L, 480, 120)));
        when(agendamentos.somarPalletsDoDia(DATA)).thenReturn(130L);
        when(agendamentos.somarPalletsPorFaixa(DATA))
                .thenReturn(Collections.singletonList(new Object[]{1L, 130L}));
        when(faixas.findAllByOrderByHoraInicioAsc()).thenReturn(List.of(
                new FaixaHoraria(1L, "08:00 - 10:00", LocalTime.of(8, 0), LocalTime.of(10, 0)),
                new FaixaHoraria(2L, "10:00 - 12:00", LocalTime.of(10, 0), LocalTime.of(12, 0))));

        var ocupacao = service.consultar(DATA);

        assertThat(ocupacao.usado()).isEqualTo(130);
        assertThat(ocupacao.limite()).isEqualTo(480);
        assertThat(ocupacao.percentual()).isEqualTo(27.1);
        assertThat(ocupacao.excedido()).isFalse();
        assertThat(ocupacao.faixas()).hasSize(2);

        var primeira = ocupacao.faixas().get(0);
        assertThat(primeira.usado()).isEqualTo(130);
        assertThat(primeira.percentual()).isEqualTo(108.3);
        assertThat(primeira.excedido()).isTrue();

        var segunda = ocupacao.faixas().get(1);
        assertThat(segunda.usado()).isZero();
        assertThat(segunda.percentual()).isZero();
        assertThat(segunda.excedido()).isFalse();
    }

    @Test
    void marcaDiaExcedido() {
        when(configuracoes.findById(ConfiguracaoCapacidade.ID_UNICO))
                .thenReturn(Optional.of(new ConfiguracaoCapacidade(1L, 480, 120)));
        when(agendamentos.somarPalletsDoDia(DATA)).thenReturn(500L);
        when(agendamentos.somarPalletsPorFaixa(DATA)).thenReturn(List.of());
        when(faixas.findAllByOrderByHoraInicioAsc()).thenReturn(List.of());

        var ocupacao = service.consultar(DATA);

        assertThat(ocupacao.excedido()).isTrue();
        assertThat(ocupacao.percentual()).isEqualTo(104.2);
        assertThat(ocupacao.faixas()).isEmpty();
    }

    @Test
    void semConfiguracaoLancaErroDeNegocio() {
        when(configuracoes.findById(ConfiguracaoCapacidade.ID_UNICO)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.consultar(DATA)).isInstanceOf(BusinessException.class);
    }
}
