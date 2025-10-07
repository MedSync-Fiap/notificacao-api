package com.medsync.notificacao.domain.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record ConsultaEditadaNotificacaoEvent(
    @JsonProperty("evento") String evento,
    @JsonProperty("consulta_id") String consultaId,
    @JsonProperty("paciente_nome") String pacienteNome,
    @JsonProperty("paciente_email") String pacienteEmail,
    @JsonProperty("paciente_telefone") String pacienteTelefone,
    @JsonProperty("medico_nome") String medicoNome,
    @JsonProperty("medico_email") String medicoEmail,
    @JsonProperty("medico_telefone") String medicoTelefone,
    @JsonProperty("medico_especialidade") String medicoEspecialidade,
    @JsonProperty("nova_data_hora") String novaDataHora,
    @JsonProperty("observacoes") String observacoes,
    @JsonProperty("status") String status,
    @JsonProperty("alteracoes") Map<String, Object> alteracoes,
    @JsonProperty("editado_por_id") String editadoPorId,
    @JsonProperty("timestamp") String timestamp
) {
    public ConsultaEditadaNotificacaoEvent(String consultaId, String pacienteNome, String pacienteEmail,
                                         String pacienteTelefone, String medicoNome, String medicoEmail,
                                         String medicoTelefone, String medicoEspecialidade, 
                                         String novaDataHora, String observacoes, String status,
                                         Map<String, Object> alteracoes, String editadoPorId, 
                                         String timestamp) {
        this("consulta_editada_notificacao", consultaId, pacienteNome, pacienteEmail, pacienteTelefone,
             medicoNome, medicoEmail, medicoTelefone, medicoEspecialidade, novaDataHora, observacoes, 
             status, alteracoes, editadoPorId, timestamp);
    }
}
