package org.venhaserjava.dto;

/**
 * Registro de dados (Record) para capturar as credenciais de autenticação.
 * O uso de Records garante imutabilidade e um contrato de entrada limpo
 * para os endpoints de segurança (AuthResource).
 * * @param username Identificador único do usuário (ex: CPF ou e-mail).
 * @param password Senha em texto simples, a ser processada pela camada de segurança.
 */
public record LoginRequest(String username, String password) {}

// package org.venhaserjava.dto;

// public record LoginRequest(String username, String password) {}

