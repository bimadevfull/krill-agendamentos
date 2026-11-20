package com.redekrill.agendamentos.domain.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CapacidadeCalculatorTest {

    @Test
    void paletizadoConsomeQuantidadeInformada() {
        assertThat(CapacidadeCalculator.palletsConsumidos(10, 1)).isEqualTo(10);
    }

    @Test
    void naoPaletizadoConsomeODobro() {
        assertThat(CapacidadeCalculator.palletsConsumidos(10, 2)).isEqualTo(20);
    }

    @Test
    void rejeitaQuantidadeInvalida() {
        assertThatThrownBy(() -> CapacidadeCalculator.palletsConsumidos(0, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CapacidadeCalculator.palletsConsumidos(-5, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejeitaFatorInvalido() {
        assertThatThrownBy(() -> CapacidadeCalculator.palletsConsumidos(5, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void calculaPercentualComUmaCasaDecimal() {
        assertThat(CapacidadeCalculator.percentual(60, 120)).isEqualTo(50.0);
        assertThat(CapacidadeCalculator.percentual(130, 480)).isEqualTo(27.1);
        assertThat(CapacidadeCalculator.percentual(130, 120)).isEqualTo(108.3);
    }

    @Test
    void percentualComLimiteZeradoEhZero() {
        assertThat(CapacidadeCalculator.percentual(10, 0)).isZero();
    }

    @Test
    void excedeSomenteQuandoUltrapassaOLimite() {
        assertThat(CapacidadeCalculator.excede(120, 120)).isFalse();
        assertThat(CapacidadeCalculator.excede(121, 120)).isTrue();
    }
}
