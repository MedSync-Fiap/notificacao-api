package com.medsync.notificacao.infrastructure.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Cliente HTTP para integração com o serviço de Cadastro
 */
@Component
public class CadastroServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(CadastroServiceClient.class);

    private final RestTemplate restTemplate;
    private final String cadastroServiceUrl;

    public CadastroServiceClient(RestTemplate restTemplate, 
                                @Value("${app.services.agendamento.url:http://localhost:8080}") String agendamentoServiceUrl) {
        this.restTemplate = restTemplate;
        this.cadastroServiceUrl = agendamentoServiceUrl;
    }

    /**
     * Busca dados completos do paciente
     */
    public UserResponse buscarPaciente(UUID pacienteId) {
        try {
            logger.debug("Buscando dados do paciente: {}", pacienteId);
            
            String url = String.format("%s/usuarios/%s", cadastroServiceUrl, pacienteId);
            
            // Adicionar header para identificar que é do serviço de notificações
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Service-Source", "notificacao-api");
            headers.set("X-Service-Port", "8082");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            UserResponse response = restTemplate.exchange(url, HttpMethod.GET, entity, UserResponse.class).getBody();
            
            if (response != null) {
                logger.debug("Dados do paciente encontrados: {} - {}", response.nome(), response.email());
            } else {
                logger.warn("Paciente não encontrado: {}", pacienteId);
            }
            
            return response;
            
        } catch (RestClientException e) {
            logger.error("Erro ao buscar dados do paciente {}: {}", pacienteId, e.getMessage());
            return null;
        }
    }

    /**
     * Busca dados completos do médico
     */
    public UserResponse buscarMedico(UUID medicoId) {
        try {
            logger.debug("Buscando dados do médico: {}", medicoId);
            
            String url = String.format("%s/usuarios/%s", cadastroServiceUrl, medicoId);
            
            // Adicionar header para identificar que é do serviço de notificações
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Service-Source", "notificacao-api");
            headers.set("X-Service-Port", "8082");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            UserResponse response = restTemplate.exchange(url, HttpMethod.GET, entity, UserResponse.class).getBody();
            
            if (response != null) {
                logger.debug("Dados do médico encontrados: {} - {}", response.nome());
            } else {
                logger.warn("Médico não encontrado: {}", medicoId);
            }
            
            return response;
            
        } catch (RestClientException e) {
            logger.error("Erro ao buscar dados do médico {}: {}", medicoId, e.getMessage());
            return null;
        }
    }

    /**
     * DTO para resposta do paciente
     */
    public record UserResponse(
        UUID id,
        String nome,
        String cpf,
        String email,
        UUID roleId,
        boolean ativo,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm,
        List<TelefoneResponse> telefones
    ) {}

    public record TelefoneResponse(
        UUID id,
        String numero,
        String tipo
    ) {}

}
