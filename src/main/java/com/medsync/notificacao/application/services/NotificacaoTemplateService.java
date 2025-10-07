package com.medsync.notificacao.application.services;

import com.medsync.notificacao.domain.events.ConsultaCriadaNotificacaoEvent;
import com.medsync.notificacao.domain.events.ConsultaEditadaNotificacaoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class NotificacaoTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacaoTemplateService.class);
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public NotificacaoTemplate gerarTemplateConsultaCriada(ConsultaCriadaNotificacaoEvent evento) {
        logger.debug("Gerando template para consulta criada: {}", evento.consultaId());
        return gerarTemplateConsultaCriada(evento.pacienteNome(), evento.medicoNome(), 
                                          evento.medicoEspecialidade(), LocalDateTime.parse(evento.dataHora()), evento.observacoes());
    }
    
    public NotificacaoTemplate gerarTemplateConsultaEditada(ConsultaEditadaNotificacaoEvent evento) {
        logger.debug("Gerando template para consulta editada: {}", evento.consultaId());
        return gerarTemplateConsultaEditada(evento.pacienteNome(), evento.medicoNome(), 
                                           evento.medicoEspecialidade(), evento.novaDataHora(), 
                                           evento.observacoes(), evento.alteracoes());
    }

    private NotificacaoTemplate gerarTemplateConsultaCriada(String pacienteNome, String medicoNome, 
                                                           String medicoEspecialidade, java.time.LocalDateTime dataHora, String observacoes) {
        String titulo = "✅ Consulta Agendada com Sucesso";
        
        String mensagem = String.format("""
            Olá, %s!
            
            Sua consulta foi agendada com sucesso:
            
            📅 Data: %s
            ⏰ Horário: %s
            👨‍⚕️ Médico: %s
            🏥 Especialidade: %s
            %s
            
            ⚠️ Importante:
            • Chegue com 15 minutos de antecedência
            • Traga um documento com foto
            • Em caso de desistência, cancele com pelo menos 24h de antecedência
            
            Em caso de dúvidas, entre em contato conosco.
            
            Atenciosamente,
            Equipe MedSync
            """,
            pacienteNome,
            dataHora.format(DATE_FORMATTER),
            dataHora.format(DateTimeFormatter.ofPattern("HH:mm")),
            medicoNome,
            medicoEspecialidade != null ? medicoEspecialidade : "Não informada",
            observacoes != null && !observacoes.trim().isEmpty() ? 
                "📝 Observações: " + observacoes + "\n" : ""
        );

        return new NotificacaoTemplate(titulo, mensagem, "CONSULTA_CRIADA");
    }

    private NotificacaoTemplate gerarTemplateConsultaEditada(String pacienteNome, String medicoNome, 
                                                           String medicoEspecialidade, String novaDataHora, 
                                                           String observacoes, Map<String, Object> alteracoes) {
        String titulo = "🔄 Consulta Atualizada";
        
        // Construir mensagem apenas com campos alterados
        StringBuilder camposAlterados = new StringBuilder();
        
        if (alteracoes != null && !alteracoes.isEmpty()) {
            camposAlterados.append("Os seguintes dados foram alterados:\n\n");
            
            if (alteracoes.containsKey("dataHora")) {
                LocalDateTime dataHora = LocalDateTime.parse(novaDataHora);
                camposAlterados.append(String.format("📅 Nova Data: %s\n", dataHora.format(DATE_FORMATTER)));
                camposAlterados.append(String.format("⏰ Novo Horário: %s\n", dataHora.format(DateTimeFormatter.ofPattern("HH:mm"))));
            }
            
            if (alteracoes.containsKey("observacoes")) {
                camposAlterados.append(String.format("📝 Observações: %s\n", 
                    observacoes != null && !observacoes.trim().isEmpty() ? observacoes : "Removidas"));
            }
            
            if (alteracoes.containsKey("status")) {
                String status = alteracoes.get("status").toString();
                String statusFormatado = formatarStatus(status);
                camposAlterados.append(String.format("📊 Status: %s\n", statusFormatado));
            }
            
            if (alteracoes.containsKey("especialidadeId")) {
                camposAlterados.append(String.format("🏥 Especialidade: %s\n", 
                    medicoEspecialidade != null ? medicoEspecialidade : "Não informada"));
            }
        } else {
            // Fallback se não houver informações de alterações
            if (novaDataHora != null && !novaDataHora.trim().isEmpty()) {
                LocalDateTime dataHora = LocalDateTime.parse(novaDataHora);
                camposAlterados.append("📅 Data: ").append(dataHora.format(DATE_FORMATTER)).append("\n");
                camposAlterados.append("⏰ Horário: ").append(dataHora.format(DateTimeFormatter.ofPattern("HH:mm"))).append("\n");
            }
            if (observacoes != null && !observacoes.trim().isEmpty()) {
                camposAlterados.append("📝 Observações: ").append(observacoes).append("\n");
            }
        }
        
        String mensagem = String.format("""
            Olá, %s!
            
            Sua consulta foi atualizada:
            
            %s
            👨‍⚕️ Médico: %s
            
            ⚠️ Importante:
            • Verifique os novos dados da consulta
            • Chegue com 15 minutos de antecedência
            • Em caso de dúvidas, entre em contato conosco
            
            Atenciosamente,
            Equipe MedSync
            """,
            pacienteNome,
            camposAlterados.toString(),
            medicoNome
        );

        return new NotificacaoTemplate(titulo, mensagem, "CONSULTA_EDITADA");
    }
    
    private String formatarStatus(String status) {
        return switch (status.toUpperCase()) {
            case "AGENDADA" -> "🟢 Agendada";
            case "CONFIRMADA" -> "✅ Confirmada";
            case "CANCELADA" -> "❌ Cancelada";
            case "REALIZADA" -> "✅ Realizada";
            case "INATIVA" -> "⏸️ Inativa";
            default -> "📋 " + status;
        };
    }


    public record NotificacaoTemplate(
        String titulo,
        String mensagem,
        String tipoNotificacao
    ) {}
}