package com.medsync.notificacao.domain.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Evento simples para notificação de consulta editada
 */
public record ConsultaEditadaNotificacaoEvent(
    @JsonProperty("evento") String evento,
    @JsonProperty("consulta_id") UUID consultaId,
    @JsonProperty("paciente_nome") String pacienteNome,
    @JsonProperty("medico_nome") String medicoNome,
    @JsonProperty("medico_especialidade") String medicoEspecialidade,
    @JsonProperty("nova_data_hora") LocalDateTime novaDataHora
) {
    public ConsultaEditadaNotificacaoEvent(UUID consultaId, String pacienteNome, String medicoNome, 
                                          String medicoEspecialidade, LocalDateTime novaDataHora) {
        this("consulta_editada_notificacao", consultaId, pacienteNome, medicoNome, medicoEspecialidade, novaDataHora);
    }
}
