package org.venhaserjava.websocket;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/notificacoes")
@ApplicationScoped
public class ArtistaWebSocket {

    // Armazena as sessões ativas de forma Thread-Safe
    Map<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session) {
        sessions.put(session.getId(), session);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        sessions.remove(session.getId());
    }

    // Método que nosso Service chamará para avisar o mundo
    public void broadcast(String message) {
        sessions.values().forEach(s -> {
            s.getAsyncRemote().sendText(message, result -> {
                if (result.getException() != null) {
                    System.out.println("Erro ao enviar mensagem via WS: " + result.getException().getMessage());
                }
            });
        });
    }
}
