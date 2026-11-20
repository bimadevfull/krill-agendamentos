package com.redekrill.agendamentos.application.service;

import com.redekrill.agendamentos.domain.exception.BusinessException;
import com.redekrill.agendamentos.domain.exception.NotFoundException;
import com.redekrill.agendamentos.domain.model.*;
import com.redekrill.agendamentos.domain.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/** Cadastros de apoio: tipos de caminhao, tipos de paletizacao, faixas de horario e capacidade. */
@Service
public class CatalogoService {

    public static final int MAX_FAIXAS = 4;
    public static final Duration DURACAO_FAIXA = Duration.ofHours(2);

    private final TipoCaminhaoRepository caminhoes;
    private final TipoPaletizacaoRepository paletizacoes;
    private final FaixaHorariaRepository faixas;
    private final ConfiguracaoCapacidadeRepository configuracoes;

    public CatalogoService(TipoCaminhaoRepository caminhoes,
                           TipoPaletizacaoRepository paletizacoes,
                           FaixaHorariaRepository faixas,
                           ConfiguracaoCapacidadeRepository configuracoes) {
        this.caminhoes = caminhoes;
        this.paletizacoes = paletizacoes;
        this.faixas = faixas;
        this.configuracoes = configuracoes;
    }

    // ---------- caminhoes ----------
    @Transactional(readOnly = true)
    public List<TipoCaminhao> listarCaminhoes() {
        return caminhoes.findAllByOrderByNomeAsc();
    }

    @Transactional
    public TipoCaminhao criarCaminhao(String nome) {
        return caminhoes.save(new TipoCaminhao(null, nome.trim()));
    }

    @Transactional
    public TipoCaminhao atualizarCaminhao(Long id, String nome) {
        TipoCaminhao t = caminhoes.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de caminhão não encontrado"));
        t.setNome(nome.trim());
        return caminhoes.save(t);
    }

    @Transactional
    public void excluirCaminhao(Long id) {
        TipoCaminhao t = caminhoes.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de caminhão não encontrado"));
        caminhoes.delete(t);
        caminhoes.flush();
    }

    // ---------- paletizacao ----------
    @Transactional(readOnly = true)
    public List<TipoPaletizacao> listarPaletizacoes() {
        return paletizacoes.findAllByOrderByNomeAsc();
    }

    @Transactional
    public TipoPaletizacao criarPaletizacao(String nome, int fator) {
        return paletizacoes.save(new TipoPaletizacao(null, nome.trim(), fator));
    }

    @Transactional
    public TipoPaletizacao atualizarPaletizacao(Long id, String nome, int fator) {
        TipoPaletizacao t = paletizacoes.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de paletização não encontrado"));
        t.setNome(nome.trim());
        t.setFatorPallets(fator);
        return paletizacoes.save(t);
    }

    @Transactional
    public void excluirPaletizacao(Long id) {
        TipoPaletizacao t = paletizacoes.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de paletização não encontrado"));
        paletizacoes.delete(t);
        paletizacoes.flush();
    }

    // ---------- faixas de horario ----------
    @Transactional(readOnly = true)
    public List<FaixaHoraria> listarFaixas() {
        return faixas.findAllByOrderByHoraInicioAsc();
    }

    @Transactional
    public FaixaHoraria criarFaixa(String descricao, LocalTime inicio, LocalTime fim) {
        if (faixas.count() >= MAX_FAIXAS) {
            throw new BusinessException("O sistema trabalha com no máximo " + MAX_FAIXAS + " faixas de horário");
        }
        validarFaixa(null, inicio, fim);
        return faixas.save(new FaixaHoraria(null, descricao.trim(), inicio, fim));
    }

    @Transactional
    public FaixaHoraria atualizarFaixa(Long id, String descricao, LocalTime inicio, LocalTime fim) {
        FaixaHoraria f = faixas.findById(id)
                .orElseThrow(() -> new NotFoundException("Faixa horária não encontrada"));
        validarFaixa(id, inicio, fim);
        f.setDescricao(descricao.trim());
        f.setHoraInicio(inicio);
        f.setHoraFim(fim);
        return faixas.save(f);
    }

    @Transactional
    public void excluirFaixa(Long id) {
        FaixaHoraria f = faixas.findById(id)
                .orElseThrow(() -> new NotFoundException("Faixa horária não encontrada"));
        faixas.delete(f);
        faixas.flush();
    }

    private void validarFaixa(Long idAtual, LocalTime inicio, LocalTime fim) {
        if (!inicio.isBefore(fim)) {
            throw new BusinessException("O horário inicial deve ser anterior ao horário final");
        }
        if (!Duration.between(inicio, fim).equals(DURACAO_FAIXA)) {
            throw new BusinessException("Cada faixa de horário deve ter exatamente 2 horas");
        }
        for (FaixaHoraria outra : faixas.findAll()) {
            if (Objects.equals(outra.getId(), idAtual)) {
                continue;
            }
            if (inicio.isBefore(outra.getHoraFim()) && fim.isAfter(outra.getHoraInicio())) {
                throw new BusinessException("A faixa informada se sobrepõe a \"" + outra.getDescricao() + "\"");
            }
        }
    }

    // ---------- capacidade ----------
    @Transactional(readOnly = true)
    public ConfiguracaoCapacidade obterCapacidade() {
        return configuracoes.findById(ConfiguracaoCapacidade.ID_UNICO)
                .orElseThrow(() -> new BusinessException("Configuração de capacidade não cadastrada"));
    }

    @Transactional
    public ConfiguracaoCapacidade atualizarCapacidade(int palletsPorDia, int palletsPorFaixa) {
        ConfiguracaoCapacidade cfg = configuracoes.findById(ConfiguracaoCapacidade.ID_UNICO)
                .orElseGet(ConfiguracaoCapacidade::new);
        cfg.setId(ConfiguracaoCapacidade.ID_UNICO);
        cfg.setPalletsPorDia(palletsPorDia);
        cfg.setPalletsPorFaixa(palletsPorFaixa);
        return configuracoes.save(cfg);
    }
}
