package com.medsync.notificacao.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Resposta padrão da API")
public record ApiResponseDto<T>(
    @Schema(description = "Indica se a operação foi bem-sucedida", example = "true")
    boolean success,
    
    @Schema(description = "Mensagem da operação", example = "Operação realizada com sucesso")
    String message,
    
    @Schema(description = "Dados da resposta")
    T data,
    
    @Schema(description = "Timestamp da resposta", example = "2025-09-28T18:30:00")
    LocalDateTime timestamp
) {
    
    public static <T> ApiResponseDto<T> success(String message, T data) {
        return new ApiResponseDto<>(true, message, data, LocalDateTime.now());
    }
    
    public static <T> ApiResponseDto<T> success(String message) {
        return new ApiResponseDto<>(true, message, null, LocalDateTime.now());
    }
    
    public static <T> ApiResponseDto<T> error(String message) {
        return new ApiResponseDto<>(false, message, null, LocalDateTime.now());
    }
    
    public static <T> ApiResponseDto<T> error(String message, T data) {
        return new ApiResponseDto<>(false, message, data, LocalDateTime.now());
    }
}