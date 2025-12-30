
package br.com.libdomain.model;

import java.util.Map;
import java.util.UUID;

/**
 * Representa um evento genérico do sistema.
 * Não conhece nenhum conceito de domínio específico.
 */
public record Event(
        UUID eventId,
        String type,
        Map<String, Object> payload
) {

    /**
     * Cria um evento genérico com um novo UUID.
     *
     * @param type    tipo do evento (ex: PAYMENT_APPROVED)
     * @param payload dados do evento
     * @return novo Event
     */
    public static Event of(String type, Map<String, Object> payload) {
        return new Event(UUID.randomUUID(), type, payload);
    }

    /**
     * Cria um evento genérico com UUID especificado (útil para testes ou replays).
     *
     * @param eventId UUID do evento
     * @param type    tipo do evento
     * @param payload dados do evento
     * @return Event
     */
    public static Event of(UUID eventId, String type, Map<String, Object> payload) {
        return new Event(eventId, type, payload);
    }
}
