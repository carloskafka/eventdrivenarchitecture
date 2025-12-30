package br.com.libdomain.router;

import br.com.libdomain.model.Event;
import br.com.libdomain.strategy.EventStrategy;

import java.util.List;

public class DefaultStrategySelector implements StrategySelector {

    private final List<EventStrategy> strategies;

    public DefaultStrategySelector(List<EventStrategy> strategies) {
        this.strategies = strategies;
    }

    @Override
    public EventStrategy select(Event event) {
        return strategies.stream()
                .filter(s -> s.supports(event))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("No strategy for event " + event.type())
                );
    }

    @Override
    public List<EventStrategy> selectAll(Event event) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(event))
                .toList();
    }
}
