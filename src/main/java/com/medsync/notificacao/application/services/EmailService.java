package com.medsync.notificacao.application.services;

import com.medsync.notificacao.presentation.dto.NotificacaoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username:}")
    private String fromEmail;
    
    @Value("${app.notificacao.email.enabled:false}")
    private boolean emailEnabled;
    
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    public void enviarEmailConsultaCriada(NotificacaoRequest notificacao) {
        if (!emailEnabled || fromEmail.isEmpty()) {
            logger.debug("Envio de email desabilitado ou email não configurado");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo("paciente@exemplo.com"); // TODO: Buscar email real do paciente
            message.setSubject(notificacao.titulo());
            message.setText(notificacao.mensagem());
            
            mailSender.send(message);
            logger.info("Email de consulta criada enviado com sucesso: {}", notificacao.consultaId());
            
        } catch (Exception e) {
            logger.error("Erro ao enviar email de consulta criada: {}", notificacao.consultaId(), e);
        }
    }
    
    public void enviarEmailConsultaEditada(NotificacaoRequest notificacao) {
        if (!emailEnabled || fromEmail.isEmpty()) {
            logger.debug("Envio de email desabilitado ou email não configurado");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo("paciente@exemplo.com"); // TODO: Buscar email real do paciente
            message.setSubject(notificacao.titulo());
            message.setText(notificacao.mensagem());
            
            mailSender.send(message);
            logger.info("Email de consulta editada enviado com sucesso: {}", notificacao.consultaId());
            
        } catch (Exception e) {
            logger.error("Erro ao enviar email de consulta editada: {}", notificacao.consultaId(), e);
        }
    }
    
    public void enviarEmailLembrete(NotificacaoRequest notificacao) {
        if (!emailEnabled || fromEmail.isEmpty()) {
            logger.debug("Envio de email desabilitado ou email não configurado");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo("paciente@exemplo.com"); // TODO: Buscar email real do paciente
            message.setSubject(notificacao.titulo());
            message.setText(notificacao.mensagem());
            
            mailSender.send(message);
            logger.info("Email de lembrete enviado com sucesso: {}", notificacao.consultaId());
            
        } catch (Exception e) {
            logger.error("Erro ao enviar email de lembrete: {}", notificacao.consultaId(), e);
        }
    }
}
