package br.com.backend.config;

import br.com.backend.adapters.out.PaymentRepository;
import br.com.backend.strategy.OrderCreatedStrategy;
import br.com.backend.strategy.PaymentApprovedStrategy;
import br.com.libdomain.strategy.EventStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    private final PaymentRepository paymentRepository;

    public DomainConfig(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Bean
    public EventStrategy paymentApprovedStrategy() {
        // inject the repository into the strategy
        return new PaymentApprovedStrategy(paymentRepository);
    }

    @Bean
    public EventStrategy orderCreatedStrategy() {
        // inject the repository into the strategy
        return new OrderCreatedStrategy(paymentRepository);
    }

}
