# MedSync - Serviço de Notificações 🚀

> **Microsserviço responsável por gerenciar notificações automáticas do sistema MedSync**

## 📋 Índice

- [Visão Geral](#visão-geral)
- [Funcionalidades](#funcionalidades)
- [Arquitetura](#arquitetura)
- [Configuração](#configuração)
- [API Endpoints](#api-endpoints)
- [Instalação e Execução](#instalação-e-execução)
- [Integração com Outros Serviços](#integração-com-outros-serviços)
- [Monitoramento](#monitoramento)

## 🎯 Visão Geral

O **Serviço de Notificações** é um componente essencial do ecossistema MedSync, responsável por processar eventos de consultas e gerar notificações personalizadas para pacientes. O serviço recebe eventos via RabbitMQ, enriquece os dados através de integrações com outros microsserviços e envia notificações através de múltiplos canais.

### Características Principais

- ✅ **Processamento em Tempo Real**: Recebe e processa eventos via RabbitMQ
- ✅ **Templates Personalizados**: Mensagens ricas e contextuais por tipo de evento
- ✅ **Integração Robusta**: Busca dados completos de pacientes e médicos
- ✅ **Múltiplos Canais**: Suporte a email, SMS e notificações push
- ✅ **Alta Disponibilidade**: Tratamento de erros e fallbacks inteligentes

## 🚀 Funcionalidades

### 1. **Processamento de Eventos**
- **Consulta Criada**: Notifica paciente sobre novo agendamento
- **Consulta Editada**: Informa sobre alterações no agendamento
- **Consulta Cancelada**: Comunica cancelamentos
- **Lembretes**: Envio automático de lembretes antes da consulta

### 2. **Templates Inteligentes**
- **Personalização por Tipo**: Cada evento tem seu template específico
- **Dados Enriquecidos**: Inclui informações completas do médico, clínica e consulta
- **Formatação Rica**: Emojis, formatação de data/hora e estrutura clara
- **Fallbacks**: Valores padrão quando dados não estão disponíveis

### 3. **Integração com Microsserviços**
- **Serviço de Agendamento**: Recebe eventos de consultas
- **Serviço de Cadastro**: Busca dados completos de pacientes e médicos
- **Serviço de Histórico**: Mantém registro de notificações enviadas

### 4. **Canais de Notificação**
- **📧 Email**: Notificações por correio eletrônico
- **📱 SMS**: Mensagens de texto (em desenvolvimento)
- **🔔 Push**: Notificações push para apps móveis (futuro)
- **💬 WhatsApp**: Integração com WhatsApp Business (futuro)

## 🏗️ Arquitetura

```
┌─────────────────┐    RabbitMQ    ┌──────────────────┐
│   Agendamento   │ ──────────────▶ │  Notificações    │
│   (Port 8080)   │                 │   (Port 8082)    │
└─────────────────┘                 └──────────────────┘
                                            │
                                            ▼
┌─────────────────┐    HTTP/REST    ┌──────────────────┐
│    Cadastro     │ ◀────────────── │  Dados Completos │
│   (Port 8081)   │                 │   Paciente/Médico│
└─────────────────┘                 └──────────────────┘
                                            │
                                            ▼
┌─────────────────┐                 ┌──────────────────┐
│   Cliente/App   │ ◀────────────── │  Notificação     │
│   (Consumidor)  │   RabbitMQ      │   Final          │
└─────────────────┘                 └──────────────────┘
```

### Componentes Principais

- **`NotificacaoEventListener`**: Consome eventos do RabbitMQ
- **`NotificacaoService`**: Lógica principal de processamento
- **`NotificacaoTemplateService`**: Geração de templates personalizados
- **`CadastroServiceClient`**: Integração com serviço de cadastro
- **`EmailService`**: Envio de emails

## ⚙️ Configuração

### Variáveis de Ambiente

```bash
# Servidor
SERVER_PORT=8082

# RabbitMQ
SPRING_RABBITMQ_HOST=localhost
SPRING_RABBITMQ_PORT=5672
SPRING_RABBITMQ_USERNAME=medsync
SPRING_RABBITMQ_PASSWORD=medsync
SPRING_RABBITMQ_VIRTUAL_HOST=/medsync

# Email (Gmail)
MAIL_USERNAME=seu-email@gmail.com
MAIL_PASSWORD=sua-senha-app

# URLs dos Microsserviços
APP_SERVICES_AGENDAMENTO_URL=http://localhost:8080
APP_SERVICES_HISTORICO_URL=http://localhost:8081
```

### Profiles Disponíveis

- **`dev`**: Desenvolvimento com logs detalhados
- **`prod`**: Produção com logs otimizados

## 🔌 API Endpoints


### Endpoints Principais

#### 1. **Enviar Notificação Manual**
```http
POST /api/v1/notificacoes/enviar
Content-Type: application/json

{
  "consultaId": "uuid",
  "pacienteNome": "João Silva",
  "medicoNome": "Dr. Maria Santos",
  "dataHora": "2025-10-15T14:30:00",
  "tipoNotificacao": "CONSULTA_CRIADA",
  "observacoes": "Consulta de rotina"
}
```

#### 2. **Consultar Notificações por Consulta**
```http
GET /api/v1/notificacoes/consulta/{consultaId}
```

#### 3. **Enviar Lembrete**
```http
POST /api/v1/notificacoes/lembrete/{consultaId}
```

#### 4. **Cancelar Notificações**
```http
POST /api/v1/notificacoes/cancelar/{consultaId}
```

#### 5. **Status do Serviço**
```http
GET /api/v1/notificacoes/status
```

## 🚀 Instalação e Execução

### Pré-requisitos

- Java 17+
- Maven 3.6+
- RabbitMQ (Docker recomendado)
- Outros microsserviços MedSync

### 1. **Clonar e Compilar**
```bash
git clone <repo-url>
cd notificacao-api
mvn clean compile
```

### 2. **Executar RabbitMQ**
```bash
docker run -d --name medsync-rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=medsync \
  -e RABBITMQ_DEFAULT_PASS=medsync \
  -e RABBITMQ_DEFAULT_VHOST=/medsync \
  rabbitmq:3-management
```

### 3. **Executar o Serviço**
```bash
# Desenvolvimento
mvn spring-boot:run

# Produção
mvn clean package
java -jar target/notificacao-api-1.0.0.jar
```

### 4. **Verificar Status**
```bash
curl http://localhost:8082/api/v1/notificacoes/status
```

## 🔗 Integração com Outros Serviços

### Fluxo de Notificação

1. **Agendamento** → Cria/edita consulta → Envia evento para RabbitMQ
2. **Notificações** → Recebe evento → Busca dados completos
3. **Notificações** → Gera template → Envia para fila do cliente
4. **Cliente/App** → Consome notificação → Exibe para paciente

### Exemplo de Evento Recebido

```json
{
  "consultaId": "5f4e6c1a-1e32-4b30-9951-06278f8678d5",
  "pacienteId": "850e8400-e29b-41d4-a716-446655440004",
  "medicoId": "850e8400-e29b-41d4-a716-446655440004",
  "criadoPorId": "850e8400-e29b-41d4-a716-446655440001",
  "dataHora": "2025-11-21T21:19:23.154",
  "status": "AGENDADA",
  "observacoes": "Consulta de rotina",
  "tipoEvento": "EDITADA",
  "timestamp": "2025-09-28T19:07:11.207883"
}
```

### Exemplo de Notificação Gerada

```json
{
  "consultaId": "5f4e6c1a-1e32-4b30-9951-06278f8678d5",
  "pacienteNome": "João Silva",
  "medicoNome": "Dr. Maria Santos",
  "dataHora": "2025-11-21T21:19:23.154",
  "tipoNotificacao": "CONSULTA_EDITADA",
  "titulo": "🔄 Consulta Atualizada",
  "mensagem": "Olá, João Silva!\n\nSua consulta foi atualizada:\n\n📅 Nova Data: 21/11/2025\n⏰ Novo Horário: 21:19\n👨‍⚕️ Médico: Dr. Maria Santos\n📍 Local: MedSync Clínica\n...",
  "timestamp": "2025-09-28T19:07:11.714"
}
```

## 📊 Monitoramento

### Health Checks
- **Health**: `http://localhost:8082/actuator/health`
- **Metrics**: `http://localhost:8082/actuator/metrics`
- **Prometheus**: `http://localhost:8082/actuator/prometheus`

### Logs Importantes

```bash
# Processamento de evento
INFO - Processando notificação de consulta editada: {consultaId}

# Integração com cadastro
DEBUG - Buscando dados do paciente: {pacienteId}
ERROR - Erro ao buscar dados do paciente: 403

# Geração de template
DEBUG - Gerando template para evento: EDITADA

# Envio para RabbitMQ
DEBUG - Publishing message on exchange [ex_notificacoes], routingKey = [notificacao.cliente.{consultaId}]
```

### Métricas RabbitMQ

```bash
# Verificar filas
docker exec medsync-rabbitmq rabbitmqctl list_queues name messages consumers -p /medsync

# Verificar conexões
docker exec medsync-rabbitmq rabbitmqctl list_connections -p /medsync

# Verificar consumers
docker exec medsync-rabbitmq rabbitmqctl list_consumers -p /medsync
```

## 🛠️ Desenvolvimento

### Estrutura do Projeto

```
src/main/java/com/medsync/notificacao/
├── application/
│   └── services/           # Lógica de negócio
├── domain/
│   └── events/            # Eventos de domínio
├── infrastructure/
│   ├── config/            # Configurações
│   ├── clients/           # Clientes HTTP
│   └── events/            # Listeners RabbitMQ
└── presentation/
    ├── controllers/       # Controllers REST
    └── dto/              # Data Transfer Objects
```

### Executar Testes

```bash
# Todos os testes
mvn test

# Testes específicos
mvn test -Dtest=NotificacaoServiceTest

# Testes de integração
mvn test -Dtest=NotificacaoIntegrationTest
```

## 📝 Changelog

### v1.0.0 (2025-09-28)
- ✅ Processamento de eventos RabbitMQ
- ✅ Templates personalizados por tipo de evento
- ✅ Integração com serviço de cadastro
- ✅ API REST completa com Swagger
- ✅ Tratamento robusto de erros
- ✅ Logs detalhados para monitoramento

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.

## 👥 Equipe

- **Desenvolvimento**: Equipe MedSync
- **Arquitetura**: Microsserviços Spring Boot
- **Infraestrutura**: Docker + RabbitMQ

---

**MedSync** - Transformando o cuidado em saúde através da tecnologia 🏥💻