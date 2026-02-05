package org.venhaserjava.websocket;

import java.util.Map;
import jakarta.websocket.*;
import org.jboss.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Servidor de WebSocket para notificações em tempo real.
 * Mantém o registro de sessões ativas para disseminação de eventos (broadcast).
 */
@ServerEndpoint("/notificacoes")
@ApplicationScoped
public class ArtistaWebSocket {

    private static final Logger LOG = Logger.getLogger(ArtistaWebSocket.class);
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        LOG.info("Nova conexão WebSocket estabelecida: " + session.getId());
        sessions.put(session.getId(), session);
    }

    @OnClose
    public void onClose(Session session) {
        LOG.info("Conexão WebSocket encerrada: " + session.getId());
        sessions.remove(session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        LOG.error("Erro na sessão WebSocket " + session.getId() + ": " + throwable.getMessage());
        sessions.remove(session.getId());
    }

    /**
     * Envia uma mensagem para todos os clientes conectados de forma assíncrona.
     * @param message Texto da notificação.
     */
    public void broadcast(String message) {
        sessions.values().forEach(s -> {
            if (s.isOpen()) {
                s.getAsyncRemote().sendText(message, result -> {
                    if (result.getException() != null) {
                        LOG.error("Falha ao entregar broadcast: " + result.getException().getMessage());
                    }
                });
            }
        });
    }
}
