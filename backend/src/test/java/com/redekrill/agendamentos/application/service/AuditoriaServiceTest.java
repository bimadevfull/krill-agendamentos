package com.redekrill.agendamentos.application.service;

import com.redekrill.agendamentos.domain.model.LogAuditoria;
import com.redekrill.agendamentos.domain.repository.LogAuditoriaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuditoriaServiceTest {

    @Mock LogAuditoriaRepository repository;

    @InjectMocks AuditoriaService service;

    private LogAuditoria log(String usuario) {
        return new LogAuditoria(null, LocalDateTime.now(), usuario, "CONSULTAR", "/api/agendamentos", null, "ip", 200);
    }

    @Test
    void registrarSalvaOsDadosInformados() {
        service.registrar("admin@krill.com", "CRIAR", "/api/agendamentos", "Novo agendamento", "127.0.0.1", 201);

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(repository).save(captor.capture());
        LogAuditoria salvo = captor.getValue();
        assertThat(salvo.getUsuario()).isEqualTo("admin@krill.com");
        assertThat(salvo.getAcao()).isEqualTo("CRIAR");
        assertThat(salvo.getStatus()).isEqualTo(201);
        assertThat(salvo.getDataHora()).isNotNull();
    }

    @Test
    void registrarLimitaOTamanhoDosCampos() {
        service.registrar("a@a.com", "CONSULTAR", "r".repeat(300), "d".repeat(900), "i".repeat(100), 200);

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getRecurso()).hasSize(200);
        assertThat(captor.getValue().getDetalhe()).hasSize(500);
        assertThat(captor.getValue().getIp()).hasSize(60);
    }

    @Test
    void registrarAceitaDetalheNulo() {
        service.registrar("a@a.com", "CONSULTAR", "/api/x", null, null, 200);

        ArgumentCaptor<LogAuditoria> captor = ArgumentCaptor.forClass(LogAuditoria.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getDetalhe()).isNull();
        assertThat(captor.getValue().getIp()).isNull();
    }

    @Test
    void listarConsultaDoInicioDoDiaAteOFimDoDiaFinal() {
        LocalDate inicio = LocalDate.of(2026, 11, 20);
        LocalDate fim = LocalDate.of(2026, 11, 21);
        when(repository.findTop500ByDataHoraBetweenOrderByDataHoraDesc(
                inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay())).thenReturn(List.of(log("admin@krill.com")));

        List<LogAuditoria> logs = service.listar(inicio, fim, null);

        assertThat(logs).hasSize(1);
    }

    @Test
    void listarSemFiltroDeUsuarioRetornaTodos() {
        when(repository.findTop500ByDataHoraBetweenOrderByDataHoraDesc(
                LocalDate.of(2026, 11, 20).atStartOfDay(), LocalDate.of(2026, 11, 21).atStartOfDay()))
                .thenReturn(List.of(log("admin@krill.com"), log("usuario@krill.com")));

        List<LogAuditoria> logs = service.listar(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 20), "   ");

        assertThat(logs).hasSize(2);
    }

    @Test
    void listarFiltraPorUsuarioSemDiferenciarMaiusculas() {
        when(repository.findTop500ByDataHoraBetweenOrderByDataHoraDesc(
                LocalDate.of(2026, 11, 20).atStartOfDay(), LocalDate.of(2026, 11, 21).atStartOfDay()))
                .thenReturn(List.of(log("admin@krill.com"), log("usuario@krill.com")));

        List<LogAuditoria> logs = service.listar(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 11, 20), " ADMIN ");

        assertThat(logs).extracting(LogAuditoria::getUsuario).containsExactly("admin@krill.com");
    }
}
