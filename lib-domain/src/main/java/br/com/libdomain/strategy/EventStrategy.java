
package br.com.libdomain.strategy;

import br.com.libdomain.model.Event;

/**
 * Estratégia para processar eventos genéricos.
 * Define um contrato para executar ações baseadas no tipo do evento.
 */
public interface EventStrategy {
    void execute(Event event);
    boolean supports(Event event);
}
