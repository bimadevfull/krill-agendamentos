package com.redekrill.agendamentos.application.service;

import com.redekrill.agendamentos.application.dto.NovoAgendamento;
import com.redekrill.agendamentos.application.port.NotificadorEmail;
import com.redekrill.agendamentos.domain.exception.BusinessException;
import com.redekrill.agendamentos.domain.exception.NotFoundException;
import com.redekrill.agendamentos.domain.model.*;
import com.redekrill.agendamentos.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AgendamentoServiceTest {

    private static final LocalDate DATA = LocalDate.of(2026, 11, 20);

    @Mock AgendamentoRepository agendamentos;
    @Mock FaixaHorariaRepository faixas;
    @Mock TipoCaminhaoRepository caminhoes;
    @Mock TipoPaletizacaoRepository paletizacoes;
    @Mock ConfiguracaoCapacidadeRepository configuracoes;
    @Mock NotificadorEmail notificador;

    @InjectMocks AgendamentoService service;

    private FaixaHoraria faixa;
    private TipoCaminhao truck;
    private TipoPaletizacao paletizado;
    private TipoPaletizacao naoPaletizado;

    @BeforeEach
    void setUp() {
        faixa = new FaixaHoraria(1L, "08:00 - 10:00", LocalTime.of(8, 0), LocalTime.of(10, 0));
        truck = new TipoCaminhao(1L, "Truck");
        paletizado = new TipoPaletizacao(1L, "Paletizado", 1);
        naoPaletizado = new TipoPaletizacao(2L, "Não Paletizado", 2);

        when(faixas.findById(1L)).thenReturn(Optional.of(faixa));
        when(caminhoes.findById(1L)).thenReturn(Optional.of(truck));
        when(paletizacoes.findById(1L)).thenReturn(Optional.of(paletizado));
        when(paletizacoes.findById(2L)).thenReturn(Optional.of(naoPaletizado));
        when(configuracoes.findById(ConfiguracaoCapacidade.ID_UNICO))
                .thenReturn(Optional.of(new ConfiguracaoCapacidade(1L, 480, 120)));
        when(agendamentos.somarPalletsDoDia(DATA)).thenReturn(0L);
        when(agendamentos.somarPalletsDaFaixa(DATA, 1L)).thenReturn(0L);
        when(agendamentos.save(any(Agendamento.class))).thenAnswer(i -> i.getArgument(0));
    }

    private NovoAgendamento comando(long paletizacaoId, int quantidade) {
        return new NovoAgendamento(DATA, 1L, 1001L, " Fornecedor X ", " contato@fornecedor.com ",
                1L, paletizacaoId, quantidade, "  ");
    }

    @Test
    void criaAgendamentoPaletizadoSemAvisos() {
        when(agendamentos.somarPalletsDoDia(DATA)).thenReturn(100L);
        when(agendamentos.somarPalletsDaFaixa(DATA, 1L)).thenReturn(50L);

        var resultado = service.criar(comando(1L, 10), "admin@krill.com");

        assertThat(resultado.avisos()).isEmpty();
        ArgumentCaptor<Agendamento> captor = ArgumentCaptor.forClass(Agendamento.class);
        verify(agendamentos).save(captor.capture());
        Agendamento salvo = captor.getValue();
        assertThat(salvo.getPalletsConsumidos()).isEqualTo(10);
        assertThat(salvo.getStatus()).isEqualTo(StatusAgendamento.AGENDADO);
        assertThat(salvo.getFornecedor()).isEqualTo("Fornecedor X");
        assertThat(salvo.getEmailFornecedor()).isEqualTo("contato@fornecedor.com");
        assertThat(salvo.getObservacao()).isNull();
        assertThat(salvo.getCriadoPor()).isEqualTo("admin@krill.com");
        assertThat(salvo.getCriadoEm()).isNotNull();
        verify(notificador).enviarConfirmacao(salvo);
    }

    @Test
    void naoPaletizadoDobraOsPalletsDaFaixa() {
        var resultado = service.criar(comando(2L, 10), "admin@krill.com");

        assertThat(resultado.agendamento().getPalletsConsumidos()).isEqualTo(20);
        assertThat(resultado.agendamento().getQuantidadePallets()).isEqualTo(10);
    }

    @Test
    void avisaQuandoFaixaExcedeMasNaoBloqueia() {
        when(agendamentos.somarPalletsDaFaixa(DATA, 1L)).thenReturn(115L);

        var resultado = service.criar(comando(1L, 10), "admin@krill.com");

        assertThat(resultado.avisos()).hasSize(1);
        assertThat(resultado.avisos().get(0)).contains("faixa").contains("125 de 120");
        verify(agendamentos).save(any(Agendamento.class));
    }

    @Test
    void avisaQuandoDiaExcedeMasNaoBloqueia() {
        when(agendamentos.somarPalletsDoDia(DATA)).thenReturn(475L);

        var resultado = service.criar(comando(1L, 10), "admin@krill.com");

        assertThat(resultado.avisos()).hasSize(1);
        assertThat(resultado.avisos().get(0)).contains("diária").contains("485 de 480");
        verify(agendamentos).save(any(Agendamento.class));
    }

    @Test
    void avisaDiaEFaixaJuntos() {
        when(agendamentos.somarPalletsDoDia(DATA)).thenReturn(475L);
        when(agendamentos.somarPalletsDaFaixa(DATA, 1L)).thenReturn(120L);

        var resultado = service.criar(comando(1L, 10), "admin@krill.com");

        assertThat(resultado.avisos()).hasSize(2);
    }

    @Test
    void mantemObservacaoQuandoInformada() {
        var cmd = new NovoAgendamento(DATA, 1L, 1L, "F", "f@f.com", 1L, 1L, 1, " Descarga lateral ");
        var resultado = service.criar(cmd, "admin@krill.com");
        assertThat(resultado.agendamento().getObservacao()).isEqualTo("Descarga lateral");
    }

    @Test
    void faixaInexistenteLancaNaoEncontrado() {
        var cmd = new NovoAgendamento(DATA, 99L, 1L, "F", "f@f.com", 1L, 1L, 1, null);
        assertThatThrownBy(() -> service.criar(cmd, "u")).isInstanceOf(NotFoundException.class);
        verify(agendamentos, never()).save(any());
        verifyNoInteractions(notificador);
    }

    @Test
    void caminhaoInexistenteLancaNaoEncontrado() {
        var cmd = new NovoAgendamento(DATA, 1L, 1L, "F", "f@f.com", 99L, 1L, 1, null);
        assertThatThrownBy(() -> service.criar(cmd, "u")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void paletizacaoInexistenteLancaNaoEncontrado() {
        var cmd = new NovoAgendamento(DATA, 1L, 1L, "F", "f@f.com", 1L, 99L, 1, null);
        assertThatThrownBy(() -> service.criar(cmd, "u")).isInstanceOf(NotFoundException.class);
    }

    @Test
    void semConfiguracaoDeCapacidadeLancaErroDeNegocio() {
        when(configuracoes.findById(ConfiguracaoCapacidade.ID_UNICO)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.criar(comando(1L, 1), "u")).isInstanceOf(BusinessException.class);
    }

    @Test
    void cancelaAgendamentoAtivo() {
        Agendamento ag = new Agendamento();
        ag.setStatus(StatusAgendamento.AGENDADO);
        when(agendamentos.findById(5L)).thenReturn(Optional.of(ag));

        Agendamento cancelado = service.cancelar(5L);

        assertThat(cancelado.getStatus()).isEqualTo(StatusAgendamento.CANCELADO);
        verify(agendamentos).save(ag);
    }

    @Test
    void naoCancelaDuasVezes() {
        Agendamento ag = new Agendamento();
        ag.setStatus(StatusAgendamento.CANCELADO);
        when(agendamentos.findById(5L)).thenReturn(Optional.of(ag));

        assertThatThrownBy(() -> service.cancelar(5L)).isInstanceOf(BusinessException.class);
        verify(agendamentos, never()).save(any());
    }

    @Test
    void obterInexistenteLancaNaoEncontrado() {
        when(agendamentos.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.obter(404L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void listarExigePeriodo() {
        assertThatThrownBy(() -> service.listar(null, DATA, null)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> service.listar(DATA, null, null)).isInstanceOf(BusinessException.class);
    }

    @Test
    void listarRejeitaPeriodoInvertido() {
        assertThatThrownBy(() -> service.listar(DATA, DATA.minusDays(1), null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void listarFiltraPorStatus() {
        Agendamento ativo = new Agendamento();
        ativo.setStatus(StatusAgendamento.AGENDADO);
        Agendamento cancelado = new Agendamento();
        cancelado.setStatus(StatusAgendamento.CANCELADO);
        when(agendamentos.buscarPorPeriodo(DATA, DATA)).thenReturn(List.of(ativo, cancelado));

        assertThat(service.listar(DATA, DATA, null)).hasSize(2);
        assertThat(service.listar(DATA, DATA, StatusAgendamento.CANCELADO)).containsExactly(cancelado);
    }
}
