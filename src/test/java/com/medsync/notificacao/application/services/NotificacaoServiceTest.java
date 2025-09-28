package com.medsync.notificacao.application.services;

import com.medsync.notificacao.domain.events.ConsultaNotificacaoEvent;
import com.medsync.notificacao.domain.gateways.UsuarioGateway;
import com.medsync.notificacao.domain.entities.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacaoServiceTest {

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

        verify(rabbitTemplate).convertAndSend(
            eq("ex_notificacoes"),
            any(String.class),
            any(Object.class)
        );

        verify(emailService).enviarEmailConsultaCriada(any());
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

        verify(rabbitTemplate).convertAndSend(
            eq("ex_notificacoes"),
            any(String.class),
            any(Object.class)
        );

        verify(emailService).enviarEmailConsultaEditada(any());
    }

    @Test
    void deveTratarExcecaoAoProcessarNotificacao() {
        // Arrange
        ConsultaNotificacaoEvent evento = new ConsultaNotificacaoEvent(
            UUID.randomUUID(),
            "João Silva",
            "Dr. Maria",
            LocalDateTime.now().plusDays(1),
            "CRIADA",
            LocalDateTime.now()
        );

        // Simular erro no template service
        when(templateService.criarNotificacaoConsultaCriada(any(), any(), any(), any()))
            .thenThrow(new RuntimeException("Erro no template"));

        // Act & Assert - Não deve lançar exceção, apenas logar o erro
        notificacaoService.processarConsultaCriada(evento);

        verify(templateService).criarNotificacaoConsultaCriada(any(), any(), any(), any());
        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class), any(Object.class));
        verify(emailService, never()).enviarEmailConsultaCriada(any());
    }
}
