package com.medsync.notificacao.application.services;

import com.medsync.notificacao.domain.events.ConsultaNotificacaoEvent;
import com.medsync.notificacao.infrastructure.clients.CadastroServiceClient;
import com.medsync.notificacao.infrastructure.events.dto.NotificacaoConsultaPayload;
import com.medsync.notificacao.presentation.dto.NotificacaoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificacaoService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificacaoService.class);
    
    private final RabbitTemplate rabbitTemplate;
    private final EmailService emailService;
    private final NotificacaoTemplateService templateService;
    private final CadastroServiceClient cadastroServiceClient;
    
    @Value("${app.rabbitmq.exchange-notificacoes}")
    private String exchangeNotificacoes;
    
    @Value("${app.rabbitmq.routing-key-cliente}")
    private String routingKeyCliente;
    
    public NotificacaoService(RabbitTemplate rabbitTemplate,
                             EmailService emailService, 
                             NotificacaoTemplateService templateService,
                             CadastroServiceClient cadastroServiceClient) {
        this.rabbitTemplate = rabbitTemplate;
        this.emailService = emailService;
        this.templateService = templateService;
        this.cadastroServiceClient = cadastroServiceClient;
    }
    
    public void processarConsultaCriada(ConsultaNotificacaoEvent evento) {
        try {
            logger.info("Processando notificação de consulta criada: {}", evento.consultaId());
            
            // Buscar dados completos do paciente e médico
            var dadosCompletos = buscarDadosCompletos(evento);
            
            // Gerar template personalizado
            var template = templateService.gerarTemplate(dadosCompletos);
            
            // Criar notificação com dados completos
            NotificacaoRequest notificacao = new NotificacaoRequest(
                evento.consultaId(),
                dadosCompletos.pacienteNome() != null ? dadosCompletos.pacienteNome() : "Paciente",
                dadosCompletos.medicoNome() != null ? dadosCompletos.medicoNome() : "Médico",
                evento.dataHora(),
                template.tipoNotificacao(),
                template.titulo(),
                template.mensagem(),
                LocalDateTime.now()
            );
            
            // Enviar notificação para fila específica do cliente
            String routingKey = "notificacao.cliente." + evento.consultaId();
            rabbitTemplate.convertAndSend(exchangeNotificacoes, routingKey, notificacao);
            
            // Enviar email (se configurado)
            emailService.enviarEmailConsultaCriada(notificacao);
            
            logger.info("Notificação de consulta criada processada com sucesso: {}", evento.consultaId());
            
        } catch (Exception e) {
            logger.error("Erro ao processar notificação de consulta criada: {}", evento.consultaId(), e);
        }
    }
    
    public void processarConsultaEditada(ConsultaNotificacaoEvent evento) {
        try {
            logger.info("Processando notificação de consulta editada: {}", evento.consultaId());
            
            // Buscar dados completos do paciente e médico
            var dadosCompletos = buscarDadosCompletos(evento);
            
            // Gerar template personalizado
            var template = templateService.gerarTemplate(dadosCompletos);
            
            // Criar notificação com dados completos
            NotificacaoRequest notificacao = new NotificacaoRequest(
                evento.consultaId(),
                dadosCompletos.pacienteNome() != null ? dadosCompletos.pacienteNome() : "Paciente",
                dadosCompletos.medicoNome() != null ? dadosCompletos.medicoNome() : "Médico",
                evento.dataHora(),
                template.tipoNotificacao(),
                template.titulo(),
                template.mensagem(),
                LocalDateTime.now()
            );
            
            // Enviar notificação para fila específica do cliente
            String routingKey = "notificacao.cliente." + evento.consultaId();
            rabbitTemplate.convertAndSend(exchangeNotificacoes, routingKey, notificacao);
            
            // Enviar email (se configurado)
            emailService.enviarEmailConsultaEditada(notificacao);
            
            logger.info("Notificação de consulta editada processada com sucesso: {}", evento.consultaId());
            
        } catch (Exception e) {
            logger.error("Erro ao processar notificação de consulta editada: {}", evento.consultaId(), e);
        }
    }
    
    /**
     * Busca dados completos do paciente, médico e clínica
     */
    private NotificacaoConsultaPayload buscarDadosCompletos(ConsultaNotificacaoEvent evento) {
        logger.debug("Buscando dados completos para consulta: {}", evento.consultaId());
        
        // Buscar dados do paciente
        var paciente = cadastroServiceClient.buscarPaciente(evento.pacienteId());
        
        // Buscar dados do médico
        var medico = cadastroServiceClient.buscarMedico(evento.medicoId());
        
        
        // Criar payload com dados completos
        return new NotificacaoConsultaPayload(
            evento.consultaId(),
            evento.pacienteId(),
            evento.medicoId(),
            evento.criadoPorId(),
            evento.dataHora(),
            evento.status(),
            evento.observacoes(),
            evento.tipoEvento(),
            evento.timestamp(),
            
            // Dados do paciente
            paciente != null ? paciente.nome() : "Paciente",
            paciente != null ? paciente.email() : null,
            paciente != null && paciente.telefones() != null && !paciente.telefones().isEmpty() 
                ? paciente.telefones().get(0).numero() : null,
            paciente != null ? paciente.cpf() : null,
            
            // Dados do médico
            medico != null ? medico.nome() : "Médico",
            medico != null ? medico.email() : null,
            medico != null && medico.telefones() != null && !medico.telefones().isEmpty() 
                ? medico.telefones().get(0).numero() : null,
            
            // Dados da clínica (valores padrão)
            "MedSync Clínica",
            "Endereço não informado",
            "(11) 99999-9999"
        );
    }
}
