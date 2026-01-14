package br.com.backend.adapters.in.kafka;

import br.com.backend.application.usecases.ProcessPaymentEventUseCase;
import br.com.backend.infrastructure.messaging.PaymentEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaPaymentListener {

    private final ProcessPaymentEventUseCase useCase;

    public KafkaPaymentListener(ProcessPaymentEventUseCase useCase) {
        this.useCase = useCase;
    }

    //@KafkaListener(topics = "payment-events") // TODO: configurar o kafka no docker-compose
    public void consume(PaymentEvent event) {
        useCase.execute(event);
    }
}
