package com.medsync.notificacao.application.services;

import com.medsync.notificacao.domain.events.ConsultaNotificacaoEvent;
import com.medsync.notificacao.domain.gateways.UsuarioGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SimpleNotificacaoTest {

    @Mock
    private UsuarioGateway usuarioGateway;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private EmailService emailService;

    @Mock
    private NotificacaoTemplateService templateService;

    private NotificacaoService notificacaoService;

    @BeforeEach
    void setUp() {
        notificacaoService = new NotificacaoService(
            usuarioGateway, 
            rabbitTemplate, 
            emailService, 
            templateService
        );
    }

    @Test
    void deveProcessarConsultaCriadaComSucesso() {
        // Arrange
        UUID consultaId = UUID.randomUUID();
        ConsultaNotificacaoEvent evento = new ConsultaNotificacaoEvent(
            consultaId,
            "João Silva",
            "Dr. Maria",
            LocalDateTime.now().plusDays(1),
            "CRIADA",
            LocalDateTime.now()
        );

        // Act
        notificacaoService.processarConsultaCriada(evento);

        // Assert
        verify(templateService).criarNotificacaoConsultaCriada(
            eq(consultaId),
            eq("João Silva"),
            eq("Dr. Maria"),
            eq(evento.dataHora())
        );
    }

    @Test
    void deveProcessarConsultaEditadaComSucesso() {
        // Arrange
        UUID consultaId = UUID.randomUUID();
        ConsultaNotificacaoEvent evento = new ConsultaNotificacaoEvent(
            consultaId,
            "João Silva",
            "Dr. Maria",
            LocalDateTime.now().plusDays(2),
            "EDITADA",
            LocalDateTime.now()
        );

        // Act
        notificacaoService.processarConsultaEditada(evento);

        // Assert
        verify(templateService).criarNotificacaoConsultaEditada(
            eq(consultaId),
            eq("João Silva"),
            eq("Dr. Maria"),
            eq(evento.dataHora())
        );
    }
}
