package br.com.libdomain.router;

import br.com.libdomain.model.Event;
import br.com.libdomain.strategy.EventStrategy;

import java.util.List;

public interface StrategySelector {
    EventStrategy select(Event event);
    List<EventStrategy> selectAll(Event event);
}
