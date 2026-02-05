
   # 🏛️ Memorial Arquitetural - Sistema de Gestão SEPLAG
Este documento detalha as decisões técnicas tomadas para garantir que a API atenda aos requisitos de alta disponibilidade, escalabilidade e segurança do edital 001/2026.

## 1. Stack Tecnológica e Paradigma Reativo
A aplicação foi construída sobre o **Java 21 e Quarkus 3.x**, utilizando o paradigma de Programação Reativa (Mutiny).

Justificativa: Diferente do modelo thread-per-request tradicional, o modelo não-bloqueante permite que a API processe milhares de conexões simultâneas com um consumo mínimo de memória, sendo ideal para o ambiente de containers (Docker) onde os recursos são limitados.

Persistência: O Hibernate Reactive foi integrado para garantir que o acesso ao PostgreSQL 16 não bloqueie o Event Loop, mantendo a latência baixa mesmo em picos de carga.

## 2. Estratégia de Segurança e RBAC
Autenticação JWT (Stateless): Implementação de tokens assinados com RSA256 (chaves de 2048 bits). O par de chaves .pem garante a autenticidade sem a necessidade de consultar o banco a cada requisição.

Autorização (RBAC): Controle de acesso granular utilizando @RolesAllowed. Operações de escrita (POST, PUT, DELETE) são restritas ao perfil ADMIN, enquanto consultas estão abertas para auditoria.

Proteção de Infraestrutura: Implementação de Rate Limiting para mitigar ataques de negação de serviço (DoS) e garantir a disponibilidade para usuários legítimos.

## 3. Gestão Reativa de Mídias (S3/MinIO)
Em vez de sobrecarregar o banco de dados com arquivos binários (BLOBs), a arquitetura utiliza o padrão de Object Storage:

O arquivo é enviado via Multipart/form-data.

O S3Service processa o upload assíncrono.

- **Pre-signed URLs:** A API retorna uma URL temporária assinada eletronicamente com validade de 30 minutos.

Benefício: Segurança máxima (o bucket permanece privado) e economia de banda (o download é feito diretamente do storage para o cliente).

## 4. Integração e Sincronização de Dados
Resilience: A comunicação com a API de Regionais da SEPLAG utiliza o MicroProfile Rest Client Reativo, com tratamento de falhas e timeouts configurados.

Eventos em Tempo Real: A integração com WebSockets (ArtistaWebSocket) permite que o ecossistema seja notificado instantaneamente sobre alterações críticas (como exclusão de artistas ou novos uploads), reduzindo a necessidade de polling constante pelo frontend.

