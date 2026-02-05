# mario-ramos-rossatti-junior-567868

# 🎵 Music API – SEPLAG 001/2026

## Sistema de Gestão Administrativa – Avaliação Técnica

**Candidato:** Mario Ramos Rossatti Junior

**Inscrição:** 16373

**Cargo Avaliado:** Engenheiro de Computação Sênior

**Edital:** SEPLAG 001/2026


---

## 📌 Visão Geral

A **Music API** é uma API REST reativa desenvolvida para a gestão de acervo musical, projetada para atender requisitos rigorosos de **segurança**, **escalabilidade**, **resiliência** e **alta performance**, conforme exigido em ambientes governamentais e no edital SEPLAG 001/2026.

A solução adota arquitetura moderna baseada em **programação não-bloqueante**, uso de **JWT com criptografia assimétrica (RSA)** e integração com **Object Storage (S3/MinIO)**, garantindo eficiência operacional e aderência a boas práticas de engenharia de software.

---

## 🏗️ Principais Características

* Arquitetura **Reativa e Não-Bloqueante**
* Segurança **Stateless com JWT (RSA256)**
* Controle de acesso baseado em papéis (**RBAC**)
* Integração com **Object Storage** (S3 / MinIO)
* Cache distribuído com **Redis**
* Sincronização automática de dados externos
* Comunicação em tempo real via **WebSocket**
* Proteção contra abuso com **Rate Limiting**

---

## 🛠️ Stack Tecnológica

* **Java 21**
* **Quarkus 3.x** (Framework Reativo)
* **Hibernate Reactive**
* **PostgreSQL 15+**
* **MinIO / AWS S3**
* **Redis**
* **SmallRye JWT**
* **Docker & Docker Compose**

---

## 🚀 Início Rápido

### Pré-requisitos

* Docker e Docker Compose
* Java 21 (para execução local sem container)

### Build da Aplicação

```bash
./mvnw package -DskipTests
```

### Subida do Ambiente

```bash
docker compose up --build
```

### Acesso à Documentação Interativa (Swagger)

```
http://localhost:8081/swagger
```

---

## 📖 Documentação Técnica

A documentação detalhada do projeto está organizada nos arquivos abaixo:

* 📐 **Arquitetura e Decisões Técnicas**
  [Acessar documento](./docs/arquitetura.md)

* 🔌 **Guia de Endpoints, Fluxos e Testes**
  [Acessar documento](./docs/api-guia.md)

Esses documentos descrevem, respectivamente, as decisões arquiteturais adotadas e o passo a passo para validação funcional da API.

---

## 🔐 Observação sobre Segurança

> **Nota ao Avaliador:**
> As chaves RSA (`.pem`) foram incluídas **exclusivamente para fins de homologação e teste** da segurança JWT, permitindo a validação imediata do mecanismo de autenticação sem configuração adicional.
> **Em ambiente produtivo, essas chaves jamais devem ser versionadas.**

---

## ✅ Conformidade com o Edital

A solução foi projetada considerando:

* Boas práticas de engenharia de software
* Arquitetura escalável e resiliente
* Segurança da informação
* Observabilidade e controle
* Clareza documental para auditoria técnica

---

## 📬 Contato

**Mario Ramos Rossatti Junior**
Engenheiro de Computação / Desenvolvedor Sênior
https://github.com/venhaserjava
https://www.linkedin.com/in/mario-ramos-rossatti-junior-471aa0246/


---

> Este repositório representa uma solução técnica completa, sustentável e aderente aos padrões exigidos para sistemas críticos da administração pública.
