package com.medsync.notificacao.application.services;

import com.medsync.notificacao.infrastructure.events.dto.NotificacaoConsultaPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Serviço responsável por gerar templates de notificação personalizados
 */
@Service
public class NotificacaoTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacaoTemplateService.class);
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Gera o template de notificação baseado no tipo de evento
     */
    public NotificacaoTemplate gerarTemplate(NotificacaoConsultaPayload payload) {
        logger.debug("Gerando template para evento: {} - Consulta: {}", payload.tipoEvento(), payload.consultaId());
        
        return switch (payload.tipoEvento()) {
            case "CRIADA" -> gerarTemplateConsultaCriada(payload);
            case "EDITADA" -> gerarTemplateConsultaEditada(payload);
            case "CANCELADA" -> gerarTemplateConsultaCancelada(payload);
            case "LEMBRETE" -> gerarTemplateLembrete(payload);
            default -> gerarTemplateGenerico(payload);
        };
    }

    /**
     * Template para consulta criada
     */
    private NotificacaoTemplate gerarTemplateConsultaCriada(NotificacaoConsultaPayload payload) {
        String titulo = "✅ Consulta Agendada com Sucesso";
        
        String mensagem = String.format("""
            Olá, %s!
            
            Sua consulta foi agendada com sucesso:
            
            📅 Data: %s
            ⏰ Horário: %s
            👨‍⚕️ Médico: %s
            📍 Local: %s
            📞 Telefone da Clínica: %s
            
            %s
            
            ⚠️ Importante:
            • Chegue com 15 minutos de antecedência
            • Traga um documento com foto
            • Em caso de desistência, cancele com pelo menos 24h de antecedência
            
            Em caso de dúvidas, entre em contato conosco.
            
            Atenciosamente,
            Equipe %s
            """,
            payload.pacienteNome(),
            payload.dataHora().format(DATE_FORMATTER),
            payload.dataHora().format(DateTimeFormatter.ofPattern("HH:mm")),
            payload.medicoNome(),
            payload.clinicaNome(),
            payload.clinicaTelefone(),
            payload.observacoes() != null && !payload.observacoes().isEmpty() 
                ? "📝 Observações: " + payload.observacoes() : "",
            payload.clinicaNome()
        );

        return new NotificacaoTemplate(titulo, mensagem, "CONSULTA_CRIADA");
    }

    /**
     * Template para consulta editada
     */
    private NotificacaoTemplate gerarTemplateConsultaEditada(NotificacaoConsultaPayload payload) {
        String titulo = "🔄 Consulta Atualizada";
        
        String mensagem = String.format("""
            Olá, %s!
            
            Sua consulta foi atualizada:
            
            📅 Nova Data: %s
            ⏰ Novo Horário: %s
            👨‍⚕️ Médico: %s
            📍 Local: %s
            📞 Telefone da Clínica: %s
            
            %s
            
            ⚠️ Importante:
            • Verifique os novos dados da consulta
            • Chegue com 15 minutos de antecedência
            • Em caso de dúvidas, entre em contato conosco
            
            Atenciosamente,
            Equipe %s
            """,
            payload.pacienteNome(),
            payload.dataHora().format(DATE_FORMATTER),
            payload.dataHora().format(DateTimeFormatter.ofPattern("HH:mm")),
            payload.medicoNome(),
            payload.clinicaNome(),
            payload.clinicaTelefone(),
            payload.observacoes() != null && !payload.observacoes().isEmpty() 
                ? "📝 Observações: " + payload.observacoes() : "",
            payload.clinicaNome()
        );

        return new NotificacaoTemplate(titulo, mensagem, "CONSULTA_EDITADA");
    }

    /**
     * Template para consulta cancelada
     */
    private NotificacaoTemplate gerarTemplateConsultaCancelada(NotificacaoConsultaPayload payload) {
        String titulo = "❌ Consulta Cancelada";
        
        String mensagem = String.format("""
            Olá, %s!
            
            Sua consulta foi cancelada:
            
            📅 Data: %s
            ⏰ Horário: %s
            👨‍⚕️ Médico: %s
            
            %s
            
            Para reagendar sua consulta, entre em contato conosco:
            📞 %s
            
            Atenciosamente,
            Equipe %s
            """,
            payload.pacienteNome(),
            payload.dataHora().format(DATE_FORMATTER),
            payload.dataHora().format(DateTimeFormatter.ofPattern("HH:mm")),
            payload.medicoNome(),
            payload.observacoes() != null && !payload.observacoes().isEmpty() 
                ? "📝 Motivo: " + payload.observacoes() : "",
            payload.clinicaTelefone(),
            payload.clinicaNome()
        );

        return new NotificacaoTemplate(titulo, mensagem, "CONSULTA_CANCELADA");
    }

    /**
     * Template para lembrete de consulta
     */
    private NotificacaoTemplate gerarTemplateLembrete(NotificacaoConsultaPayload payload) {
        String titulo = "⏰ Lembrete: Sua Consulta é Amanhã";
        
        String mensagem = String.format("""
            Olá, %s!
            
            Este é um lembrete da sua consulta:
            
            📅 Data: %s
            ⏰ Horário: %s
            👨‍⚕️ Médico: %s
            📍 Local: %s
            📞 Telefone da Clínica: %s
            
            %s
            
            ⚠️ Lembre-se:
            • Chegue com 15 minutos de antecedência
            • Traga um documento com foto
            • Em caso de desistência, cancele imediatamente
            
            Esperamos vê-lo(a)!
            
            Atenciosamente,
            Equipe %s
            """,
            payload.pacienteNome(),
            payload.dataHora().format(DATE_FORMATTER),
            payload.dataHora().format(DateTimeFormatter.ofPattern("HH:mm")),
            payload.medicoNome(),
            payload.clinicaNome(),
            payload.clinicaTelefone(),
            payload.observacoes() != null && !payload.observacoes().isEmpty() 
                ? "📝 Observações: " + payload.observacoes() : "",
            payload.clinicaNome()
        );

        return new NotificacaoTemplate(titulo, mensagem, "LEMBRETE");
    }

    /**
     * Template genérico para outros tipos de evento
     */
    private NotificacaoTemplate gerarTemplateGenerico(NotificacaoConsultaPayload payload) {
        String titulo = "📋 Notificação de Consulta";
        
        String mensagem = String.format("""
            Olá, %s!
            
            Você recebeu uma notificação sobre sua consulta:
            
            📅 Data: %s
            ⏰ Horário: %s
            👨‍⚕️ Médico: %s
            📍 Local: %s
            
            %s
            
            Para mais informações, entre em contato conosco.
            
            Atenciosamente,
            Equipe %s
            """,
            payload.pacienteNome(),
            payload.dataHora().format(DATE_FORMATTER),
            payload.dataHora().format(DateTimeFormatter.ofPattern("HH:mm")),
            payload.medicoNome(),
            payload.clinicaNome(),
            payload.observacoes() != null && !payload.observacoes().isEmpty() 
                ? "📝 Observações: " + payload.observacoes() : "",
            payload.clinicaNome()
        );

        return new NotificacaoTemplate(titulo, mensagem, "GENERICO");
    }

    /**
     * Record para representar um template de notificação
     */
    public record NotificacaoTemplate(
        String titulo,
        String mensagem,
        String tipoNotificacao
    ) {}
}