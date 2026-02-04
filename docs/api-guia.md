# 🚀 Guia de Uso da API

## Fluxo de Teste Recomendado

### 1. Autenticação
**POST** `/v1/auth/login`
```json
{ "username": "admin", "password": "admin123" }

Copie o accessToken retornado.

2. Gestão de Mídia (Upload de Capa)
Crie um Artista e um Álbum (IDs 1).

Use o endpoint POST /v1/albuns/1/capa enviando um arquivo via form-data.

O retorno será uma URL do MinIO.

Validação: A URL expira em 30 minutos. Tente acessá-la no navegador.

3. Rate Limit
Tente acessar o endpoint /v1/artistas mais de 10 vezes em 1 minuto. Você receberá um status 429 Too Many Requests