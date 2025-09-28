package com.medsync.notificacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class NotificacaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificacaoApplication.class, args);
    }
}
