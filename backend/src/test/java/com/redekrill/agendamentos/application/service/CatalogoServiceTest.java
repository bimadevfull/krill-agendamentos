package com.redekrill.agendamentos.application.service;

import com.redekrill.agendamentos.domain.exception.BusinessException;
import com.redekrill.agendamentos.domain.exception.NotFoundException;
import com.redekrill.agendamentos.domain.model.ConfiguracaoCapacidade;
import com.redekrill.agendamentos.domain.model.FaixaHoraria;
import com.redekrill.agendamentos.domain.model.TipoCaminhao;
import com.redekrill.agendamentos.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CatalogoServiceTest {

    @Mock TipoCaminhaoRepository caminhoes;
    @Mock TipoPaletizacaoRepository paletizacoes;
    @Mock FaixaHorariaRepository faixas;
    @Mock ConfiguracaoCapacidadeRepository configuracoes;

    @InjectMocks CatalogoService service;

    @BeforeEach
    void setUp() {
        when(faixas.save(any(FaixaHoraria.class))).thenAnswer(i -> i.getArgument(0));
        when(configuracoes.save(any(ConfiguracaoCapacidade.class))).thenAnswer(i -> i.getArgument(0));
        when(caminhoes.save(any(TipoCaminhao.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void criaFaixaDeDuasHoras() {
        when(faixas.count()).thenReturn(0L);
        when(faixas.findAll()).thenReturn(List.of());

        FaixaHoraria f = service.criarFaixa(" 08:00 - 10:00 ", LocalTime.of(8, 0), LocalTime.of(10, 0));

        assertThat(f.getDescricao()).isEqualTo("08:00 - 10:00");
    }

    @Test
    void rejeitaFaixaQueNaoTemDuasHoras() {
        when(faixas.count()).thenReturn(0L);
        assertThatThrownBy(() -> service.criarFaixa("x", LocalTime.of(8, 0), LocalTime.of(9, 0)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("2 horas");
    }

    @Test
    void rejeitaHorarioInicialPosteriorAoFinal() {
        when(faixas.count()).thenReturn(0L);
        assertThatThrownBy(() -> service.criarFaixa("x", LocalTime.of(10, 0), LocalTime.of(8, 0)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void rejeitaFaixaSobreposta() {
        when(faixas.count()).thenReturn(1L);
        when(faixas.findAll()).thenReturn(List.of(
                new FaixaHoraria(1L, "08:00 - 10:00", LocalTime.of(8, 0), LocalTime.of(10, 0))));
        assertThatThrownBy(() -> service.criarFaixa("x", LocalTime.of(9, 0), LocalTime.of(11, 0)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("sobrepõe");
    }

    @Test
    void rejeitaQuintaFaixa() {
        when(faixas.count()).thenReturn(4L);
        assertThatThrownBy(() -> service.criarFaixa("x", LocalTime.of(17, 0), LocalTime.of(19, 0)))
                .isInstanceOf(BusinessException.class).hasMessageContaining("4 faixas");
    }

    @Test
    void atualizaFaixaIgnorandoASiMesmaNaSobreposicao() {
        FaixaHoraria existente = new FaixaHoraria(1L, "08:00 - 10:00", LocalTime.of(8, 0), LocalTime.of(10, 0));
        when(faixas.findById(1L)).thenReturn(Optional.of(existente));
        when(faixas.findAll()).thenReturn(List.of(existente));

        FaixaHoraria f = service.atualizarFaixa(1L, "Manhã cedo", LocalTime.of(7, 0), LocalTime.of(9, 0));

        assertThat(f.getHoraInicio()).isEqualTo(LocalTime.of(7, 0));
        assertThat(f.getDescricao()).isEqualTo("Manhã cedo");
    }

    @Test
    void atualizarFaixaInexistenteLancaNaoEncontrado() {
        when(faixas.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.atualizarFaixa(9L, "x", LocalTime.of(8, 0), LocalTime.of(10, 0)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void excluirCaminhaoForcaFlushParaDetectarVinculos() {
        TipoCaminhao t = new TipoCaminhao(1L, "Truck");
        when(caminhoes.findById(1L)).thenReturn(Optional.of(t));

        service.excluirCaminhao(1L);

        verify(caminhoes).delete(t);
        verify(caminhoes).flush();
    }

    @Test
    void excluirCaminhaoInexistenteLancaNaoEncontrado() {
        when(caminhoes.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.excluirCaminhao(1L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void criaCaminhaoComNomeAparado() {
        assertThat(service.criarCaminhao("  Bitrem ").getNome()).isEqualTo("Bitrem");
    }

    @Test
    void atualizaCapacidadeExistente() {
        ConfiguracaoCapacidade cfg = new ConfiguracaoCapacidade(1L, 480, 120);
        when(configuracoes.findById(1L)).thenReturn(Optional.of(cfg));

        ConfiguracaoCapacidade r = service.atualizarCapacidade(600, 150);

        assertThat(r.getPalletsPorDia()).isEqualTo(600);
        assertThat(r.getPalletsPorFaixa()).isEqualTo(150);
    }

    @Test
    void criaCapacidadeQuandoAindaNaoExiste() {
        when(configuracoes.findById(1L)).thenReturn(Optional.empty());

        ConfiguracaoCapacidade r = service.atualizarCapacidade(480, 120);

        assertThat(r.getId()).isEqualTo(ConfiguracaoCapacidade.ID_UNICO);
    }

    @Test
    void obterCapacidadeSemRegistroLancaErroDeNegocio() {
        when(configuracoes.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.obterCapacidade()).isInstanceOf(BusinessException.class);
    }
}
