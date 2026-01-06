package br.com.backend.adapters.in;

import br.com.backend.application.usecases.ProcessPaymentEventUseCase;
import br.com.backend.model.payment.PaymentStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Kafka inbound adapter that listens for payment events and forwards them to the use case.
 * Assumes messages are JSON with fields: eventId (UUID), paymentId (String), status (String).
 */
@Component
public class KafkaPaymentListener {

    private final ProcessPaymentEventUseCase useCase;
    private final ObjectMapper mapper = new ObjectMapper();

    public KafkaPaymentListener(ProcessPaymentEventUseCase useCase) {
        this.useCase = useCase;
    }

    @KafkaListener(topics = "payment-events", groupId = "backend-group")
    public void onMessage(ConsumerRecord<String, String> record) {
        try {
            String payload = record.value();
            PaymentEventDto dto = mapper.readValue(payload, PaymentEventDto.class);
            UUID eventId = dto.getEventId();
            String paymentId = dto.getPaymentId();
            PaymentStatus status = PaymentStatus.valueOf(dto.getStatus());

            System.out.println("[KAFKA] Received event " + eventId + " for payment " + paymentId + " -> " + status);
            useCase.execute(eventId, paymentId, status);
        } catch (Exception e) {
            System.err.println("Failed to process kafka message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

