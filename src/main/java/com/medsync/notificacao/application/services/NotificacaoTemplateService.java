package com.medsync.notificacao.application.services;

import com.medsync.notificacao.domain.events.ConsultaCriadaNotificacaoEvent;
import com.medsync.notificacao.domain.events.ConsultaEditadaNotificacaoEvent;
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
     * Gera o template de notificação para consulta criada
     */
    public NotificacaoTemplate gerarTemplateConsultaCriada(ConsultaCriadaNotificacaoEvent evento) {
        logger.debug("Gerando template para consulta criada: {}", evento.consultaId());
        return gerarTemplateConsultaCriada(evento.pacienteNome(), evento.medicoNome(), 
                                          evento.medicoEspecialidade(), evento.dataHora());
    }
    
    /**
     * Gera o template de notificação para consulta editada
     */
    public NotificacaoTemplate gerarTemplateConsultaEditada(ConsultaEditadaNotificacaoEvent evento) {
        logger.debug("Gerando template para consulta editada: {}", evento.consultaId());
        return gerarTemplateConsultaEditada(evento.pacienteNome(), evento.medicoNome(), 
                                           evento.medicoEspecialidade(), evento.novaDataHora());
    }

    /**
     * Template para consulta criada
     */
    private NotificacaoTemplate gerarTemplateConsultaCriada(String pacienteNome, String medicoNome, 
                                                           String medicoEspecialidade, java.time.LocalDateTime dataHora) {
        String titulo = "✅ Consulta Agendada com Sucesso";
        
        String mensagem = String.format("""
            Olá, %s!
            
            Sua consulta foi agendada com sucesso:
            
            📅 Data: %s
            ⏰ Horário: %s
            👨‍⚕️ Médico: %s
            🏥 Especialidade: %s
            
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
            medicoEspecialidade != null ? medicoEspecialidade : "Não informada"
        );

        return new NotificacaoTemplate(titulo, mensagem, "CONSULTA_CRIADA");
    }

    /**
     * Template para consulta editada
     */
    private NotificacaoTemplate gerarTemplateConsultaEditada(String pacienteNome, String medicoNome, 
                                                           String medicoEspecialidade, java.time.LocalDateTime novaDataHora) {
        String titulo = "🔄 Consulta Atualizada";
        
        String mensagem = String.format("""
            Olá, %s!
            
            Sua consulta foi atualizada:
            
            📅 Nova Data: %s
            ⏰ Novo Horário: %s
            👨‍⚕️ Médico: %s
            🏥 Especialidade: %s
            
            ⚠️ Importante:
            • Verifique os novos dados da consulta
            • Chegue com 15 minutos de antecedência
            • Em caso de dúvidas, entre em contato conosco
            
            Atenciosamente,
            Equipe MedSync
            """,
            pacienteNome,
            novaDataHora.format(DATE_FORMATTER),
            novaDataHora.format(DateTimeFormatter.ofPattern("HH:mm")),
            medicoNome,
            medicoEspecialidade != null ? medicoEspecialidade : "Não informada"
        );

        return new NotificacaoTemplate(titulo, mensagem, "CONSULTA_EDITADA");
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