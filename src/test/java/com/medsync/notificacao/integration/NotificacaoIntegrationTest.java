package com.medsync.notificacao.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medsync.notificacao.application.services.NotificacaoService;
import com.medsync.notificacao.domain.events.ConsultaNotificacaoEvent;
import com.medsync.notificacao.infrastructure.persistence.entities.UsuarioEntity;
import com.medsync.notificacao.infrastructure.persistence.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class NotificacaoIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("medsync_notificacao_test")
            .withUsername("test_user")
            .withPassword("test_pass");

    @Container
    static RabbitMQContainer rabbitMQ = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3-management"))
            .withExposedPorts(5672, 15672);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        
        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);
        registry.add("spring.rabbitmq.port", () -> rabbitMQ.getMappedPort(5672));
        registry.add("spring.rabbitmq.username", () -> "medsync");
        registry.add("spring.rabbitmq.password", () -> "medsync");
        registry.add("spring.rabbitmq.virtual-host", () -> "/medsync");
        
        registry.add("app.notificacao.email.enabled", () -> "false");
    }

    @Autowired
    private NotificacaoService notificacaoService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @MockBean
    private JavaMailSender mailSender;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Limpar dados de teste
        usuarioRepository.deleteAll();
        
        // Criar usuário de teste
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(UUID.randomUUID());
        usuario.setNome("João Silva");
        usuario.setEmail("joao@teste.com");
        usuario.setCpf("12345678901");
        usuario.setAtivo(true);
        usuario.setCriadoEm(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    @Test
    void deveProcessarEventoConsultaCriada() throws Exception {
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

        CountDownLatch messageLatch = new CountDownLatch(1);
        final Message[] receivedMessage = {null};

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(rabbitTemplate.getConnectionFactory());
        container.setQueueNames("q_notificacoes_cliente");
        container.setMessageListener(message -> {
            synchronized (this) {
                receivedMessage[0] = message;
                messageLatch.countDown();
            }
        });
        container.start();

        // Act
        notificacaoService.processarConsultaCriada(evento);

        // Assert
        boolean messageReceived = messageLatch.await(5, TimeUnit.SECONDS);
        assertThat(messageReceived).isTrue();

        if (receivedMessage[0] != null) {
            String messageBody = new String(receivedMessage[0].getBody());
            System.out.println("Notificação enviada: " + messageBody);
            
            assertThat(messageBody).contains("CONSULTA_CRIADA");
            assertThat(messageBody).contains("João Silva");
            assertThat(messageBody).contains("Dr. Maria");
        }

        container.stop();
    }

    @Test
    void deveProcessarEventoConsultaEditada() throws Exception {
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

        CountDownLatch messageLatch = new CountDownLatch(1);
        final Message[] receivedMessage = {null};

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(rabbitTemplate.getConnectionFactory());
        container.setQueueNames("q_notificacoes_cliente");
        container.setMessageListener(message -> {
            synchronized (this) {
                receivedMessage[0] = message;
                messageLatch.countDown();
            }
        });
        container.start();

        // Act
        notificacaoService.processarConsultaEditada(evento);

        // Assert
        boolean messageReceived = messageLatch.await(5, TimeUnit.SECONDS);
        assertThat(messageReceived).isTrue();

        if (receivedMessage[0] != null) {
            String messageBody = new String(receivedMessage[0].getBody());
            System.out.println("Notificação enviada: " + messageBody);
            
            assertThat(messageBody).contains("CONSULTA_EDITADA");
            assertThat(messageBody).contains("João Silva");
            assertThat(messageBody).contains("Dr. Maria");
        }

        container.stop();
    }

    @Test
    void deveReceberMensagemDoRabbitMQ() throws Exception {
        // Arrange
        UUID consultaId = UUID.randomUUID();
        String mensagemJson = String.format("""
            {
                "evento": "consulta_criada_notificacao",
                "consulta_id": "%s",
                "paciente_nome": "João Silva",
                "medico_nome": "Dr. Maria",
                "data_hora": "%s"
            }
            """, consultaId, LocalDateTime.now().plusDays(1));

        CountDownLatch messageLatch = new CountDownLatch(1);
        final Message[] receivedMessage = {null};

        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(rabbitTemplate.getConnectionFactory());
        container.setQueueNames("q_notificacoes_cliente");
        container.setMessageListener(message -> {
            synchronized (this) {
                receivedMessage[0] = message;
                messageLatch.countDown();
            }
        });
        container.start();

        // Act - Enviar mensagem diretamente para a fila de notificações
        rabbitTemplate.convertAndSend("q_notificacoes_consultas", mensagemJson);

        // Assert
        boolean messageReceived = messageLatch.await(10, TimeUnit.SECONDS);
        assertThat(messageReceived).isTrue();

        if (receivedMessage[0] != null) {
            String messageBody = new String(receivedMessage[0].getBody());
            System.out.println("Notificação processada: " + messageBody);
            
            assertThat(messageBody).contains("CONSULTA_CRIADA");
        }

        container.stop();
    }
}
