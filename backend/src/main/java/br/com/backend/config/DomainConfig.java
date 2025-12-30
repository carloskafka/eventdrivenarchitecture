
package br.com.backend.config;

import br.com.backend.adapters.out.PaymentRepository;
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
        // injeta o repository na strategy
        return new PaymentApprovedStrategy(paymentRepository);
    }

}
