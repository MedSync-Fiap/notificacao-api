package com.medsync.notificacao.domain.events;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConsultaNotificacaoEvent(
    UUID consultaId,
    UUID pacienteId,
    UUID medicoId,
    UUID criadoPorId,
    LocalDateTime dataHora,
    String status,
    String observacoes,
    String tipoEvento, // "CRIADA" ou "EDITADA"
    LocalDateTime timestamp
) {
    // Métodos de conveniência para compatibilidade
    public String pacienteNome() {
        return "Paciente " + pacienteId.toString().substring(0, 8);
    }
    
    public String medicoNome() {
        return "Dr. " + medicoId.toString().substring(0, 8);
    }
}
