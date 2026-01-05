package br.com.libdomain.router;

import br.com.libdomain.model.Event;
import br.com.libdomain.strategy.EventStrategy;

import java.util.List;

/**
 * Implementação padrão do seletor de estratégias.
 * Seleciona todas as estratégias que suportam o evento fornecido.
 */
public class DefaultStrategySelector implements StrategySelector {

    private final List<EventStrategy> strategies;

    public DefaultStrategySelector(List<EventStrategy> strategies) {
        this.strategies = strategies;
    }

    @Override
    public List<EventStrategy> selectAll(Event event) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(event))
                .toList();
    }
}
