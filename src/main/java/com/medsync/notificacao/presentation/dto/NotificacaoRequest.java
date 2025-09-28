package com.medsync.notificacao.presentation.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificacaoRequest(
    UUID consultaId,
    String pacienteNome,
    String medicoNome,
    LocalDateTime dataHora,
    String tipoNotificacao, // "CONSULTA_CRIADA", "CONSULTA_EDITADA", "LEMBRETE"
    String titulo,
    String mensagem,
    LocalDateTime timestamp
) {}
