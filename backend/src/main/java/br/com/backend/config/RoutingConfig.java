package br.com.backend.config;

import br.com.libdomain.router.DefaultStrategySelector;
import br.com.libdomain.router.EventRouter;
import br.com.libdomain.router.StrategySelector;
import br.com.libdomain.strategy.EventStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for setting up the event routing mechanism.
 */
@Configuration
public class RoutingConfig {

    @Bean
    public StrategySelector strategySelector(List<EventStrategy> strategies) {
        return new DefaultStrategySelector(strategies);
    }

    @Bean
    public EventRouter eventRouter(StrategySelector selector) {
        return new EventRouter(selector);
    }
}