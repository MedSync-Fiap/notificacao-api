package com.medsync.notificacao.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class RabbitMQConfig {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConfig.class);

    @Value("${app.rabbitmq.exchange-notificacoes}")
    private String exchangeNotificacoes;

    @Value("${spring.rabbitmq.host}")
    private String rabbitHost;

    @Value("${spring.rabbitmq.port}")
    private int rabbitPort;

    @Value("${spring.rabbitmq.username}")
    private String rabbitUsername;

    @Value("${spring.rabbitmq.virtual-host}")
    private String rabbitVirtualHost;

    @Value("${app.rabbitmq.queue-notificacoes}")
    private String queueNotificacoes;

    @Value("${app.rabbitmq.queue-notificacoes-cliente}")
    private String queueNotificacoesCliente;

    @Value("${app.rabbitmq.routing-key-notificacoes}")
    private String routingKeyNotificacoes;

    @Value("${app.rabbitmq.routing-key-cliente}")
    private String routingKeyCliente;

    @PostConstruct
    public void logRabbitMQConfiguration() {
        logger.info("=== RabbitMQ Configuration ===");
        logger.info("Host: {}", rabbitHost);
        logger.info("Port: {}", rabbitPort);
        logger.info("Username: {}", rabbitUsername);
        logger.info("Virtual Host: {}", rabbitVirtualHost);
        logger.info("Exchange: {}", exchangeNotificacoes);
        logger.info("Queue Notificacoes: {}", queueNotificacoes);
        logger.info("Queue Cliente: {}", queueNotificacoesCliente);
        logger.info("==============================");
    }

    @Bean
    public TopicExchange exchangeNotificacoes() {
        return new TopicExchange(exchangeNotificacoes);
    }

    @Bean
    public Queue filaNotificacoes() {
        return QueueBuilder.durable(queueNotificacoes).build();
    }

    @Bean
    public Queue filaNotificacoesCliente() {
        return QueueBuilder.durable(queueNotificacoesCliente).build();
    }

    @Bean
    public Binding bindingNotificacoes() {
        return BindingBuilder
                .bind(filaNotificacoes())
                .to(exchangeNotificacoes())
                .with(routingKeyNotificacoes);
    }

    @Bean
    public Binding bindingNotificacoesCliente() {
        return BindingBuilder
                .bind(filaNotificacoesCliente())
                .to(exchangeNotificacoes())
                .with(routingKeyCliente);
    }

    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Configuração para aceitar tipos de diferentes pacotes
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setCreateMessageIds(true);
        
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        return factory;
    }
}
