package com.medsync.notificacao.infrastructure.events;

import com.medsync.notificacao.application.services.NotificacaoService;
import com.medsync.notificacao.domain.events.ConsultaNotificacaoEvent;
import com.medsync.notificacao.infrastructure.events.dto.NotificacaoConsultaPayload;
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
            
            // Tentar deserializar a mensagem
            NotificacaoConsultaPayload payload = parseMessage(messageBody);
            
            logger.info("Processando notificação para consulta: {}", payload.consultaId());
            
            // Converter o payload para o evento de domínio
            ConsultaNotificacaoEvent evento = new ConsultaNotificacaoEvent(
                payload.consultaId(),
                payload.pacienteId(),
                payload.medicoId(),
                payload.criadoPorId(),
                payload.dataHora(),
                payload.status(),
                payload.observacoes(),
                payload.tipoEvento(),
                payload.timestamp()
            );
            
            // Processar baseado no tipo de evento
            switch (payload.tipoEvento()) {
                case "CRIADA" -> {
                    logger.info("Processando evento de consulta criada: {}", evento.consultaId());
                    notificacaoService.processarConsultaCriada(evento);
                }
                case "EDITADA" -> {
                    logger.info("Processando evento de consulta editada: {}", evento.consultaId());
                    notificacaoService.processarConsultaEditada(evento);
                }
                default -> {
                    logger.warn("Tipo de evento não reconhecido: {}", payload.tipoEvento());
                }
            }
            
        } catch (Exception e) {
            logger.error("Erro ao processar mensagem de notificação", e);
            throw e; // Re-throw para que o RabbitMQ possa lidar com a mensagem (DLX, retry, etc.)
        }
    }
    
    private NotificacaoConsultaPayload parseMessage(String messageBody) {
        try {
            // Usar Jackson diretamente para deserializar
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(messageBody, NotificacaoConsultaPayload.class);
        } catch (Exception e) {
            logger.error("Erro ao fazer parse da mensagem: {}", messageBody, e);
            throw new RuntimeException("Falha ao deserializar mensagem", e);
        }
    }
}
