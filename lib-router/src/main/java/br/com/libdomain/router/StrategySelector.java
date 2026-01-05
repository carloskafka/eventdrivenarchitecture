package br.com.libdomain.router;

import br.com.libdomain.model.Event;
import br.com.libdomain.strategy.EventStrategy;

import java.util.List;

/**
 * Seleciona estratégias apropriadas para um evento específico.
 */
public interface StrategySelector {
    List<EventStrategy> selectAll(Event event);
}
