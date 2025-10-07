package com.medsync.notificacao.domain.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

public record ConsultaCriadaNotificacaoEvent(
    @JsonProperty("evento") String evento,
    @JsonProperty("consulta_id") String consultaId,
    @JsonProperty("paciente_nome") String pacienteNome,
    @JsonProperty("paciente_email") String pacienteEmail,
    @JsonProperty("paciente_telefone") String pacienteTelefone,
    @JsonProperty("medico_nome") String medicoNome,
    @JsonProperty("medico_email") String medicoEmail,
    @JsonProperty("medico_telefone") String medicoTelefone,
    @JsonProperty("medico_especialidade") String medicoEspecialidade,
    @JsonProperty("data_hora") String dataHora,
    @JsonProperty("observacoes") String observacoes,
    @JsonProperty("status") String status,
    @JsonProperty("timestamp") String timestamp
) {
    public ConsultaCriadaNotificacaoEvent(String consultaId, String pacienteNome, String pacienteEmail, 
                                         String pacienteTelefone, String medicoNome, String medicoEmail,
                                         String medicoTelefone, String medicoEspecialidade, 
                                         String dataHora, String observacoes, String status, 
                                         String timestamp) {
        this("consulta_criada_notificacao", consultaId, pacienteNome, pacienteEmail, pacienteTelefone,
             medicoNome, medicoEmail, medicoTelefone, medicoEspecialidade, dataHora, observacoes, status, timestamp);
    }
}
