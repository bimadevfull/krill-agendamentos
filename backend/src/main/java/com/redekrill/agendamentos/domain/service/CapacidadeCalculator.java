package com.redekrill.agendamentos.domain.service;

/**
 * Regras puras de capacidade (sem dependencia de framework).
 * Os limites de dia/faixa sao apenas informativos: excede-los gera aviso, nunca bloqueio.
 */
public final class CapacidadeCalculator {

    private CapacidadeCalculator() {
    }

    /** Pallets ocupados = quantidade informada x fator da paletizacao (nao paletizado = 2). */
    public static int palletsConsumidos(int quantidade, int fatorPaletizacao) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade de pallets deve ser maior que zero");
        }
        if (fatorPaletizacao <= 0) {
            throw new IllegalArgumentException("O fator de paletização deve ser maior que zero");
        }
        return quantidade * fatorPaletizacao;
    }

    /** Percentual de ocupacao com uma casa decimal. Limite zerado resulta em 0%. */
    public static double percentual(long usado, long limite) {
        if (limite <= 0) {
            return 0.0;
        }
        return Math.round(usado * 1000.0 / limite) / 10.0;
    }

    public static boolean excede(long usado, long limite) {
        return usado > limite;
    }
}
