
package br.com.libdomain.router;

import br.com.libdomain.model.Event;
import br.com.libdomain.strategy.EventStrategy;

import java.util.List;

public class EventRouter {

    private final StrategySelector selector;

    public EventRouter(StrategySelector selector) {
        this.selector = selector;
    }

    public void route(Event event) {
        List<EventStrategy> strategies = selector.selectAll(event);

        if (strategies.isEmpty()) {
            throw new IllegalStateException(
                    "No strategies found for event type=" + event.type()
            );
        }

        strategies.forEach(strategy -> strategy.execute(event));
    }
}
