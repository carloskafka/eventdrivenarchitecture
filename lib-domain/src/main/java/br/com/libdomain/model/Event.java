
package br.com.libdomain.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Representa um evento genérico do sistema.
 * Não conhece nenhum conceito de domínio específico.
 */
public class Event{
     private UUID eventId;
     private String type;
     private Map<String, Object> payload;
     private Instant occurredAt;

    protected Event() {
    }

    protected Event(UUID eventId, String type, Map<String, Object> payload, Instant occurredAt) {
        this.eventId = eventId;
        this.type = type;
        this.payload = payload;
        this.occurredAt = occurredAt;
    }

    /**
     * Cria um evento genérico com um novo UUID.
     *
     * @param type    tipo do evento (ex: PAYMENT_APPROVED)
     * @param payload dados do evento
     * @return novo Event
     */
    public static Event of(String type, Map<String, Object> payload, Instant occurredAt) {
        return new Event(UUID.randomUUID(), type, payload, occurredAt);
    }

    /**
     * Cria um evento genérico com UUID especificado (útil para testes ou replays).
     *
     * @param eventId UUID do evento
     * @param type    tipo do evento
     * @param payload dados do evento
     * @return Event
     */
    public static Event of(UUID eventId, String type, Map<String, Object> payload, Instant occurredAt) {
        return new Event(eventId, type, payload, occurredAt);
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getType() {
        return type;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
