package com.medsync.notificacao.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange-consultas}")
    private String exchangeConsultas;

    @Value("${app.rabbitmq.queue-notificacoes}")
    private String queueNotificacoes;

    @Value("${app.rabbitmq.routing-key-notificacoes}")
    private String routingKeyNotificacoes;

    // Exchange para consultas
    @Bean
    public TopicExchange exchangeConsultas() {
        return new TopicExchange(exchangeConsultas, true, false);
    }

    // Fila para notificações
    @Bean
    public Queue queueNotificacoes() {
        return QueueBuilder.durable(queueNotificacoes).build();
    }

    // Binding para notificações
    @Bean
    public Binding bindingNotificacoes() {
        return BindingBuilder
                .bind(queueNotificacoes())
                .to(exchangeConsultas())
                .with(routingKeyNotificacoes);
    }

    // Configuração do MessageConverter simples para evitar conflitos de tipos
    @Bean
    public org.springframework.amqp.support.converter.MessageConverter messageConverter() {
        return new org.springframework.amqp.support.converter.SimpleMessageConverter();
    }

    // Configuração do RabbitTemplate
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, org.springframework.amqp.support.converter.MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}