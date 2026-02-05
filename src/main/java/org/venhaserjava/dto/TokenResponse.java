package org.venhaserjava.dto;


/**
 * Representação do par de tokens gerados após uma autenticação bem-sucedida.
 * Segue o padrão da especificação OAuth2/JWT, fornecendo um token de 
 * acesso e um token de renovação (refresh) para manter a sessão do usuário.
 * * @param accessToken Token JWT assinado para autorização nas chamadas da API.
 * @param refreshToken Token utilizado para obter um novo accessToken sem exigir novo login.
 */
public record TokenResponse(String accessToken, String refreshToken) {}



