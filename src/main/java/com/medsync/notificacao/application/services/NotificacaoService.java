package com.medsync.notificacao.application.services;

import com.medsync.notificacao.domain.events.ConsultaCriadaNotificacaoEvent;
import com.medsync.notificacao.domain.events.ConsultaEditadaNotificacaoEvent;
import com.medsync.notificacao.infrastructure.clients.CadastroServiceClient;
import com.medsync.notificacao.infrastructure.events.dto.NotificacaoConsultaPayload;
import com.medsync.notificacao.presentation.dto.NotificacaoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificacaoService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificacaoService.class);
    
    private final RabbitTemplate rabbitTemplate;
    private final EmailService emailService;
    private final NotificacaoTemplateService templateService;
    private final CadastroServiceClient cadastroServiceClient;
    
    @Value("${app.rabbitmq.exchange-consultas}")
    private String exchangeConsultas;
    
    @Value("${app.rabbitmq.routing-key-cliente}")
    private String routingKeyCliente;
    
    public NotificacaoService(RabbitTemplate rabbitTemplate,
                             @Lazy EmailService emailService, 
                             NotificacaoTemplateService templateService,
                             CadastroServiceClient cadastroServiceClient) {
        this.rabbitTemplate = rabbitTemplate;
        this.emailService = emailService;
        this.templateService = templateService;
        this.cadastroServiceClient = cadastroServiceClient;
    }
    
    public void processarConsultaCriada(ConsultaCriadaNotificacaoEvent evento) {
        try {
            logger.info("Processando notificação de consulta criada: {}", evento.consultaId());
            
            // Gerar template personalizado
            var template = templateService.gerarTemplateConsultaCriada(evento);
            
            // Criar notificação com dados do evento
            NotificacaoRequest notificacao = new NotificacaoRequest(
                evento.consultaId(),
                dadosCompletos.pacienteNome() != null ? dadosCompletos.pacienteNome() : "Paciente",
                dadosCompletos.pacienteEmail() != null ? dadosCompletos.pacienteEmail() : "",
                dadosCompletos.pacienteTelefone() != null ? dadosCompletos.pacienteTelefone() : "",
                dadosCompletos.medicoNome() != null ? dadosCompletos.medicoNome() : "Médico",
                dadosCompletos.medicoEmail() != null ? dadosCompletos.medicoEmail() : "",
                dadosCompletos.medicoTelefone() != null ? dadosCompletos.medicoTelefone() : "",
                evento.dataHora(),
                template.tipoNotificacao(),
                template.titulo(),
                template.mensagem(),
                LocalDateTime.now()
            );
            
            // Enviar notificação para fila específica do cliente
            String routingKey = "notificacao.cliente." + evento.consultaId();
            rabbitTemplate.convertAndSend(exchangeConsultas, routingKey, notificacao);
            
            // Enviar email (se configurado)
            emailService.enviarEmailConsultaCriada(notificacao);
            
            logger.info("Notificação de consulta criada processada com sucesso: {}", evento.consultaId());
            
        } catch (Exception e) {
            logger.error("Erro ao processar notificação de consulta criada: {}", evento.consultaId(), e);
        }
    }
    
    public void processarConsultaEditada(ConsultaEditadaNotificacaoEvent evento) {
        try {
            logger.info("Processando notificação de consulta editada: {}", evento.consultaId());
            
            // Gerar template personalizado
            var template = templateService.gerarTemplateConsultaEditada(evento);
            
            // Criar notificação com dados do evento
            NotificacaoRequest notificacao = new NotificacaoRequest(
                evento.consultaId(),
                dadosCompletos.pacienteNome() != null ? dadosCompletos.pacienteNome() : "Paciente",
                dadosCompletos.pacienteEmail() != null ? dadosCompletos.pacienteEmail() : "",
                dadosCompletos.pacienteTelefone() != null ? dadosCompletos.pacienteTelefone() : "",
                dadosCompletos.medicoNome() != null ? dadosCompletos.medicoNome() : "Médico",
                dadosCompletos.medicoEmail() != null ? dadosCompletos.medicoEmail() : "",
                dadosCompletos.medicoTelefone() != null ? dadosCompletos.medicoTelefone() : "",
                evento.dataHora(),
                template.tipoNotificacao(),
                template.titulo(),
                template.mensagem(),
                LocalDateTime.now()
            );
            
            // Enviar notificação para fila específica do cliente
            String routingKey = "notificacao.cliente." + evento.consultaId();
            rabbitTemplate.convertAndSend(exchangeConsultas, routingKey, notificacao);
            
            // Enviar email (se configurado)
            emailService.enviarEmailConsultaEditada(notificacao);
            
            logger.info("Notificação de consulta editada processada com sucesso: {}", evento.consultaId());
            
        } catch (Exception e) {
            logger.error("Erro ao processar notificação de consulta editada: {}", evento.consultaId(), e);
        }
    }
    
}
