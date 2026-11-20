package com.redekrill.agendamentos.presentation.dto;

import java.util.List;

public record ApiError(int status, String message, List<String> erros) {
    public static ApiError of(int status, String message) {
        return new ApiError(status, message, List.of());
    }
}
