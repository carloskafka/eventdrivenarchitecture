package br.com.backend.application.usecases;

import br.com.backend.application.ports.out.DomainEventPublisher;
import br.com.backend.application.ports.out.PaymentRepository;
import br.com.backend.domain.payment.Payment;
import br.com.backend.infrastructure.messaging.PaymentEvent;
import org.springframework.stereotype.Component;

@Component
public class ProcessPaymentEventUseCase {

    private final PaymentRepository paymentRepository;
    private final DomainEventPublisher publisher;

    public ProcessPaymentEventUseCase(
            PaymentRepository paymentRepository,
            DomainEventPublisher publisher
    ) {
        this.paymentRepository = paymentRepository;
        this.publisher = publisher;
    }

    public void execute(PaymentEvent event) {
        System.out.println("[UseCase] Processando evento " + event.eventId() +
                " status " + event.targetStatus() + " para payment " + event.paymentId());

        Payment payment = paymentRepository
                .findById(event.paymentId())
                .orElseGet(() -> Payment.newPayment(event.paymentId(), event.orderId()));

        boolean changed = payment.applyEvent(event.eventId(), event.targetStatus(), event.occurredAt());

        if (!changed) {
            if (payment.isPending(event.eventId())) {
                System.out.println("[UseCase] Evento " + event.eventId() + " armazenado como pendente (fora de ordem)");
            } else {
                System.out.println("[UseCase] Evento " + event.eventId() + " ignorado (idempotente)");
            }
            return;
        }

        paymentRepository.save(payment);
        System.out.println("[UseCase] Estado atualizado para payment " + payment.getPaymentId() +
                ": " + payment.getStatus());

        // publicar DomainEvents
        payment.pullDomainEvents().forEach(de -> {
            System.out.println("[UseCase] Publicando DomainEvent: " + de.getClass().getSimpleName());
            publisher.publish(de);
        });
    }

}
