package com.medsync.notificacao.infrastructure.events.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificacaoConsultaPayload(
    @JsonProperty("consultaId") UUID consultaId,
    @JsonProperty("pacienteId") UUID pacienteId,
    @JsonProperty("medicoId") UUID medicoId,
    @JsonProperty("criadoPorId") UUID criadoPorId,
    @JsonProperty("dataHora") LocalDateTime dataHora,
    @JsonProperty("status") String status,
    @JsonProperty("observacoes") String observacoes,
    @JsonProperty("tipoEvento") String tipoEvento,
    @JsonProperty("timestamp") LocalDateTime timestamp,
    
    // Dados completos do paciente
    @JsonProperty("pacienteNome") String pacienteNome,
    @JsonProperty("pacienteEmail") String pacienteEmail,
    @JsonProperty("pacienteTelefone") String pacienteTelefone,
    @JsonProperty("pacienteCpf") String pacienteCpf,
    
    // Dados completos do médico
    @JsonProperty("medicoNome") String medicoNome,
    @JsonProperty("medicoEmail") String medicoEmail,
    @JsonProperty("medicoTelefone") String medicoTelefone,
    
    // Dados da clínica/consultório
    @JsonProperty("clinicaNome") String clinicaNome,
    @JsonProperty("clinicaEndereco") String clinicaEndereco,
    @JsonProperty("clinicaTelefone") String clinicaTelefone
) {
    
    @JsonCreator
    public NotificacaoConsultaPayload(
            UUID consultaId,
            UUID pacienteId,
            UUID medicoId,
            UUID criadoPorId,
            LocalDateTime dataHora,
            String status,
            String observacoes,
            String tipoEvento,
            LocalDateTime timestamp,
            String pacienteNome,
            String pacienteEmail,
            String pacienteTelefone,
            String pacienteCpf,
            String medicoNome,
            String medicoEmail,
            String medicoTelefone,
            String clinicaNome,
            String clinicaEndereco,
            String clinicaTelefone) {
        this.consultaId = consultaId;
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.criadoPorId = criadoPorId;
        this.dataHora = dataHora;
        this.status = status;
        this.observacoes = observacoes;
        this.tipoEvento = tipoEvento;
        this.timestamp = timestamp;
        this.pacienteNome = pacienteNome;
        this.pacienteEmail = pacienteEmail;
        this.pacienteTelefone = pacienteTelefone;
        this.pacienteCpf = pacienteCpf;
        this.medicoNome = medicoNome;
        this.medicoEmail = medicoEmail;
        this.medicoTelefone = medicoTelefone;
        this.clinicaNome = clinicaNome;
        this.clinicaEndereco = clinicaEndereco;
        this.clinicaTelefone = clinicaTelefone;
    }
}
