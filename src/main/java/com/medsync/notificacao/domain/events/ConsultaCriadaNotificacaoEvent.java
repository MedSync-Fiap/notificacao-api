package com.medsync.notificacao.domain.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento simples para notificação de consulta criada
 */
public record ConsultaCriadaNotificacaoEvent(
    @JsonProperty("evento") String evento,
    @JsonProperty("consulta_id") UUID consultaId,
    @JsonProperty("paciente_nome") String pacienteNome,
    @JsonProperty("medico_nome") String medicoNome,
    @JsonProperty("medico_especialidade") String medicoEspecialidade,
    @JsonProperty("data_hora") LocalDateTime dataHora
) {
    public ConsultaCriadaNotificacaoEvent(UUID consultaId, String pacienteNome, String medicoNome, 
                                         String medicoEspecialidade, LocalDateTime dataHora) {
        this("consulta_criada_notificacao", consultaId, pacienteNome, medicoNome, medicoEspecialidade, dataHora);
    }
}
