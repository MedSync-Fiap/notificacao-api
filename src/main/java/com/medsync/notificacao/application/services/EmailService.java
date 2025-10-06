package com.medsync.notificacao.application.services;

import com.medsync.notificacao.presentation.dto.NotificacaoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:}")
    private String fromEmail;
    
    @Value("${app.notificacao.email.enabled:false}")
    private boolean emailEnabled;
    
    @Value("${app.notificacao.email.from-name:MedSync}")
    private String fromName;
    
    @Value("${app.notificacao.email.retry-attempts:3}")
    private int retryAttempts;
    
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    /**
     * Envia email de consulta criada de forma assíncrona
     */
    public void enviarEmailConsultaCriada(NotificacaoRequest notificacao) {
        logger.info("Verificando configuração de email - emailEnabled: {}, fromEmail: '{}'", emailEnabled, fromEmail);
        if (!emailEnabled || !StringUtils.hasText(fromEmail)) {
            logger.warn("Envio de email desabilitado ou email não configurado - emailEnabled: {}, fromEmail: '{}'", emailEnabled, fromEmail);
            return;
        }
        
        if (!StringUtils.hasText(notificacao.pacienteEmail())) {
            logger.warn("Email do paciente não disponível para consulta: {}", notificacao.consultaId());
            return;
        }
        
        CompletableFuture.runAsync(() -> {
            enviarEmailComRetry(
                notificacao.pacienteEmail(),
                "✅ " + notificacao.titulo(),
                gerarTemplateEmailHtml(notificacao, "criada"),
                "CONSULTA_CRIADA",
                notificacao.consultaId().toString()
            );
        });
    }
    
    /**
     * Envia email de consulta editada de forma assíncrona
     */
    public void enviarEmailConsultaEditada(NotificacaoRequest notificacao) {
        if (!emailEnabled || !StringUtils.hasText(fromEmail)) {
            logger.debug("Envio de email desabilitado ou email não configurado");
            return;
        }
        
        if (!StringUtils.hasText(notificacao.pacienteEmail())) {
            logger.warn("Email do paciente não disponível para consulta: {}", notificacao.consultaId());
            return;
        }
        
        CompletableFuture.runAsync(() -> {
            enviarEmailComRetry(
                notificacao.pacienteEmail(),
                "🔄 " + notificacao.titulo(),
                gerarTemplateEmailHtml(notificacao, "editada"),
                "CONSULTA_EDITADA",
                notificacao.consultaId().toString()
            );
        });
    }
    
    /**
     * Envia email de lembrete de forma assíncrona
     */
    public void enviarEmailLembrete(NotificacaoRequest notificacao) {
        if (!emailEnabled || !StringUtils.hasText(fromEmail)) {
            logger.debug("Envio de email desabilitado ou email não configurado");
            return;
        }
        
        if (!StringUtils.hasText(notificacao.pacienteEmail())) {
            logger.warn("Email do paciente não disponível para consulta: {}", notificacao.consultaId());
            return;
        }
        
        CompletableFuture.runAsync(() -> {
            enviarEmailComRetry(
                notificacao.pacienteEmail(),
                "⏰ " + notificacao.titulo(),
                gerarTemplateEmailHtml(notificacao, "lembrete"),
                "LEMBRETE",
                notificacao.consultaId().toString()
            );
        });
    }
    
    /**
     * Envia email com retry automático
     */
    private void enviarEmailComRetry(String to, String subject, String htmlContent, String tipoEvento, String consultaId) {
        int tentativas = 0;
        Exception ultimoErro = null;
        
        while (tentativas < retryAttempts) {
            try {
                enviarEmail(to, subject, htmlContent);
                logger.info("Email {} enviado com sucesso para {} (consulta: {})", tipoEvento, to, consultaId);
                return;
                
            } catch (Exception e) {
                ultimoErro = e;
                tentativas++;
                logger.warn("Tentativa {} de {} falhou para email {} (consulta: {}): {}", 
                    tentativas, retryAttempts, tipoEvento, consultaId, e.getMessage());
                
                if (tentativas < retryAttempts) {
                    try {
                        Thread.sleep(2000 * tentativas); // Backoff exponencial
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        logger.error("Falha ao enviar email {} após {} tentativas (consulta: {}): {}", 
            tipoEvento, retryAttempts, consultaId, ultimoErro != null ? ultimoErro.getMessage() : "Erro desconhecido");
    }
    
    /**
     * Envia email HTML
     */
    private void enviarEmail(String to, String subject, String htmlContent) throws MessagingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (UnsupportedEncodingException e) {
            throw new MessagingException("Erro de encoding UTF-8", e);
        }
    }
    
    /**
     * Gera template HTML responsivo para email
     */
    private String gerarTemplateEmailHtml(NotificacaoRequest notificacao, String tipoEvento) {
        String corPrimaria = getCorPorTipoEvento(tipoEvento);
        String icone = getIconePorTipoEvento(tipoEvento);
        
        return String.format("""
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; margin: 0; padding: 0; background-color: #f8f9fa; }
                    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); }
                    .header { background: linear-gradient(135deg, %s 0%%, %s 100%%); color: white; padding: 30px; text-align: center; }
                    .header h1 { margin: 0; font-size: 24px; font-weight: 600; }
                    .content { padding: 30px; line-height: 1.6; color: #333333; }
                    .info-card { background-color: #f8f9fa; border-left: 4px solid %s; padding: 20px; margin: 20px 0; border-radius: 4px; }
                    .info-row { display: flex; justify-content: space-between; margin: 10px 0; padding: 8px 0; border-bottom: 1px solid #e9ecef; }
                    .info-row:last-child { border-bottom: none; }
                    .info-label { font-weight: 600; color: #495057; }
                    .info-value { color: #212529; }
                    .footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #6c757d; font-size: 14px; }
                    .button { display: inline-block; background-color: %s; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: 500; margin: 20px 0; }
                    .alert { background-color: #fff3cd; border: 1px solid #ffeaa7; color: #856404; padding: 15px; border-radius: 4px; margin: 15px 0; }
                    @media (max-width: 600px) { .container { margin: 10px; border-radius: 0; } .header, .content { padding: 20px; } .info-row { flex-direction: column; } }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s %s</h1>
                        <p>Olá, %s!</p>
                    </div>
                    
                    <div class="content">
                        <div class="info-card">
                            <div class="info-row">
                                <span class="info-label">📅 Data da Consulta:</span>
                                <span class="info-value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">⏰ Horário:</span>
                                <span class="info-value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">👨‍⚕️ Médico:</span>
                                <span class="info-value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">📍 Local:</span>
                                <span class="info-value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">📞 Telefone:</span>
                                <span class="info-value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="info-label">🏥 Clínica:</span>
                                <span class="info-value">%s</span>
                            </div>
                        </div>
                        
                        %s
                        
                        <div class="alert">
                            <strong>⚠️ Importante:</strong> Em caso de dúvidas ou necessidade de reagendamento, entre em contato conosco com antecedência.
                        </div>
                    </div>
                    
                    <div class="footer">
                        <p>Este é um email automático do sistema MedSync.</p>
                        <p>Para sua segurança, não responda a este email.</p>
                        <p>© 2025 MedSync - Transformando o cuidado em saúde</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            notificacao.titulo(),
            corPrimaria, escurecerCor(corPrimaria),
            corPrimaria,
            corPrimaria,
            icone, notificacao.titulo(),
            notificacao.pacienteNome(),
            formatarDataHora(notificacao.dataHora().toString()),
            formatarHora(notificacao.dataHora().toString()),
            notificacao.medicoNome(),
            "MedSync Clínica - Centro Médico",
            notificacao.medicoTelefone(),
            "MedSync Clínica",
            gerarMensagemPersonalizada(tipoEvento)
        );
    }
    
    private String getCorPorTipoEvento(String tipoEvento) {
        return switch (tipoEvento.toLowerCase()) {
            case "criada" -> "#28a745";
            case "editada" -> "#ffc107";
            case "lembrete" -> "#17a2b8";
            default -> "#007bff";
        };
    }
    
    private String getIconePorTipoEvento(String tipoEvento) {
        return switch (tipoEvento.toLowerCase()) {
            case "criada" -> "✅";
            case "editada" -> "🔄";
            case "lembrete" -> "⏰";
            default -> "📧";
        };
    }
    
    private String escurecerCor(String cor) {
        // Simplificação - em produção usar uma biblioteca de cores
        return cor.replace("#28a745", "#1e7e34")
                  .replace("#ffc107", "#e0a800")
                  .replace("#17a2b8", "#117a8b")
                  .replace("#007bff", "#0056b3");
    }
    
    private String formatarDataHora(String dataHora) {
        try {
            java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(dataHora);
            return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return dataHora;
        }
    }
    
    private String formatarHora(String dataHora) {
        try {
            java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(dataHora);
            return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            return dataHora;
        }
    }
    
    private String gerarMensagemPersonalizada(String tipoEvento) {
        return switch (tipoEvento.toLowerCase()) {
            case "criada" -> "<p>Sua consulta foi <strong>agendada com sucesso</strong>! Chegue com 15 minutos de antecedência.</p>";
            case "editada" -> "<p>Sua consulta foi <strong>atualizada</strong>. Verifique os novos dados acima.</p>";
            case "lembrete" -> "<p><strong>Lembrete:</strong> Sua consulta é hoje! Não esqueça de trazer seus documentos.</p>";
            default -> "";
        };
    }
}
