package com.redekrill.agendamentos.application.service;

import com.redekrill.agendamentos.domain.model.LogAuditoria;
import com.redekrill.agendamentos.domain.repository.LogAuditoriaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditoriaService {

    private final LogAuditoriaRepository repository;

    public AuditoriaService(LogAuditoriaRepository repository) {
        this.repository = repository;
    }

    public void registrar(String usuario, String acao, String recurso, String detalhe, String ip, Integer status) {
        LogAuditoria log = new LogAuditoria(null, LocalDateTime.now(), usuario, acao,
                limitar(recurso, 200), limitar(detalhe, 500), limitar(ip, 60), status);
        repository.save(log);
    }

    public List<LogAuditoria> listar(LocalDate inicio, LocalDate fim, String usuario) {
        List<LogAuditoria> logs = repository.findTop500ByDataHoraBetweenOrderByDataHoraDesc(
                inicio.atStartOfDay(), fim.plusDays(1).atStartOfDay());
        if (usuario == null || usuario.isBlank()) {
            return logs;
        }
        String filtro = usuario.trim().toLowerCase();
        return logs.stream().filter(l -> l.getUsuario().toLowerCase().contains(filtro)).toList();
    }

    private static String limitar(String texto, int max) {
        if (texto == null) {
            return null;
        }
        return texto.length() <= max ? texto : texto.substring(0, max);
    }
}
