package com.medsync.notificacao.infrastructure.events;

import com.medsync.notificacao.application.services.NotificacaoService;
import com.medsync.notificacao.domain.events.ConsultaCriadaNotificacaoEvent;
import com.medsync.notificacao.domain.events.ConsultaEditadaNotificacaoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class NotificacaoEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificacaoEventListener.class);
    
    private final NotificacaoService notificacaoService;
    
    public NotificacaoEventListener(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }
    
    @RabbitListener(queues = "${app.rabbitmq.queue-notificacoes}")
    public void handleConsultaNotificacao(org.springframework.amqp.core.Message message) {
        try {
            // Extrair o corpo da mensagem como String
            String messageBody = new String(message.getBody());
            logger.info("Recebida mensagem de notificação: {}", messageBody);
            logger.debug("Headers da mensagem: {}", message.getMessageProperties().getHeaders());
            
            // Tentar deserializar a mensagem baseado no tipo de evento
            String evento = extractEventType(messageBody);
            
            switch (evento) {
                case "consulta_criada_notificacao" -> {
                    ConsultaCriadaNotificacaoEvent eventoCriada = parseConsultaCriada(messageBody);
                    logger.info("Processando evento de consulta criada: {}", eventoCriada.consultaId());
                    notificacaoService.processarConsultaCriada(eventoCriada);
                }
                case "consulta_editada_notificacao" -> {
                    ConsultaEditadaNotificacaoEvent eventoEditada = parseConsultaEditada(messageBody);
                    logger.info("Processando evento de consulta editada: {}", eventoEditada.consultaId());
                    notificacaoService.processarConsultaEditada(eventoEditada);
                }
                default -> {
                    logger.warn("Tipo de evento não reconhecido: {}", evento);
                }
            }
            
        } catch (Exception e) {
            logger.error("Erro ao processar mensagem de notificação", e);
            throw e; // Re-throw para que o RabbitMQ possa lidar com a mensagem (DLX, retry, etc.)
        }
    }
    
    private String extractEventType(String messageBody) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(messageBody);
            return jsonNode.get("evento").asText();
        } catch (Exception e) {
            logger.error("Erro ao extrair tipo de evento da mensagem: {}", messageBody, e);
            throw new RuntimeException("Falha ao extrair tipo de evento", e);
        }
    }
    
    private ConsultaCriadaNotificacaoEvent parseConsultaCriada(String messageBody) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(messageBody, ConsultaCriadaNotificacaoEvent.class);
        } catch (Exception e) {
            logger.error("Erro ao fazer parse da mensagem de consulta criada: {}", messageBody, e);
            throw new RuntimeException("Falha ao deserializar mensagem de consulta criada", e);
        }
    }
    
    private ConsultaEditadaNotificacaoEvent parseConsultaEditada(String messageBody) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(messageBody, ConsultaEditadaNotificacaoEvent.class);
        } catch (Exception e) {
            logger.error("Erro ao fazer parse da mensagem de consulta editada: {}", messageBody, e);
            throw new RuntimeException("Falha ao deserializar mensagem de consulta editada", e);
        }
    }
}
