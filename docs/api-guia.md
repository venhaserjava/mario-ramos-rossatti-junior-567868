# 🚀 Guia de Uso da API

Este guia fornece os passos necessários para validar as principais funcionalidades da aplicação, respeitando as camadas de segurança e os requisitos do edital.

## Fluxo de Teste Recomendado

### 🔑 1. Autenticação (JWT)
O sistema utiliza chaves RSA para assinatura de tokens. O tempo de expiração é de 5 minutos, conforme exigido.

Endpoint: POST /v1/auth/login

Payload:

{
  "username": "admin",
  "password": "admin123"
}


`Comando via Terminal:`

curl -X POST http://localhost:8081/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin", "password":"admin123"}'

**Nota: Guarde o accessToken. Ele deve ser enviado no cabeçalho Authorization: Bearer <TOKEN> para todas as rotas protegidas (ADMIN).**


### 🖼️ 2. Ciclo de Vida da Capa (S3 & URLs Assinadas)
O upload de capas integra o sistema com o Object Storage (MinIO/S3).

**Passo 1:** Upload e Vínculo Endpoint: POST /capas/upload/{albumId}

Tipo: multipart/form-data

`Comando via Terminal:`

curl -X POST http://localhost:8081/capas/upload/1 \
     -H "Authorization: Bearer <SEU_TOKEN>" \
     -F "file=@/caminho/para/sua/imagem.jpg"
**Passo 2:** 
    Validação da URL Temporária A API retornará o objeto Álbum atualizado com a capaUrl.
    Tente acessar a URL no navegador.
    *Verificação Sênior:* Aguarde 30 minutos e tente acessar novamente. O S3 retornará Access Denied, validando o requisito de expiração da assinatura.


### 🔄 3. Sincronização de Regionais (Scheduled)
A sincronização ocorre automaticamente a cada 1 hora, mas pode ser disparada manualmente para auditoria.

Endpoint: POST /v1/regionais/sync

`Comando via Terminal:`

curl -X POST http://localhost:8081/v1/regionais/sync \
     -H "Authorization: Bearer <SEU_TOKEN>"

### 📡 4. Monitoramento em Tempo Real (WebSocket)
Para validar o broadcast de eventos, utilize um cliente **WebSocket** (ex: `Insomnia, Postman ou extensão do Chrome`).

URL: ws://localhost:8081/notificacoes

Ao realizar o upload de uma capa ou deletar um artista, você verá mensagens como:

ARTISTA_REMOVIDO: ID 1

CAPA_ATUALIZADA: Album ID 1

### 🛡️ 5. Resiliência e Rate Limit
A API possui proteção contra excesso de requisições por IP para garantir estabilidade.

Teste de Estresse: Tente realizar mais de 10 requisições seguidas no endpoint de artistas. Resposta esperada: HTTP 429 Too Many Requests.

