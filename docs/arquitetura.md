# 🏛️ Memorial Arquitetural

Este documento detalha as decisões técnicas tomadas para garantir que a API atenda aos requisitos de alta disponibilidade e segurança do edital.

## 1. Stack Tecnológica
- **Java 21 & Quarkus Reativo:** Escolhidos para maximizar o rendimento de I/O através do modelo de eventos não-bloqueantes (Mutiny).
- **Hibernate Reactive:** Persistência assíncrona para evitar gargalos de thread em operações de banco de dados.

## 2. Estratégia de Segurança
- **JWT com Chaves RSA:** Implementação de par de chaves (pública/privada) para garantir que apenas o nosso servidor de autenticação possa emitir tokens válidos.
- **Rate Limiting por IP:** Filtro customizado que intercepta requisições antes do processamento pesado, protegendo a infraestrutura contra ataques de DoS.

## 3. Gestão de Arquivos (Storage)
- **S3 Presigned URLs:** Em vez de trafegar bytes de imagem pela API, geramos URLs temporárias de 30 minutos. 
    - *Benefício:* Redução drástica de consumo de banda da API e aumento da segurança dos objetos.