package br.com.backend.adapters.in.startup;

import br.com.backend.application.ports.out.OrderRepository;
import br.com.backend.application.usecases.ProcessPaymentEventUseCase;
import br.com.backend.domain.order.Order;
import br.com.backend.domain.payment.PaymentStatus;
import br.com.backend.infrastructure.messaging.PaymentEvent;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class PaymentStartupRunner implements CommandLineRunner {

    private final ProcessPaymentEventUseCase useCase;
    private final OrderRepository orderRepository;

    public PaymentStartupRunner(ProcessPaymentEventUseCase useCase, OrderRepository orderRepository) {
        this.useCase = useCase;
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(String... args) {
        System.out.println("StartupRunner iniciado");

        // 1️⃣ Criar order de teste
        Order order = new Order("order-1");
        orderRepository.save(order);
        System.out.println("Order criada: " + order.getOrderId());

        // 2️⃣ Criar PaymentEvents de teste
        UUID e1 = UUID.randomUUID();
        UUID e2 = UUID.randomUUID();
        UUID e3 = UUID.randomUUID();
        UUID e4 = UUID.randomUUID();

        List<PaymentEvent> events = List.of(
                // Evento normal (CREATED)
                new PaymentEvent(e1, "payment-1", "order-1", PaymentStatus.CREATED, Instant.now()),

                // Evento fora de ordem (APPROVED chega antes do AUTHORIZED)
                new PaymentEvent(e2, "payment-1", "order-1", PaymentStatus.APPROVED, Instant.now().plusSeconds(1)),

                // Evento que deveria ser aplicado primeiro (AUTHORIZED)
                new PaymentEvent(e3, "payment-1", "order-1", PaymentStatus.AUTHORIZED, Instant.now().plusSeconds(2))
        );

        System.out.println("Eventos carregados: " + events.size());

        // 3️⃣ Processar eventos
        for (PaymentEvent event : events) {
            System.out.println("Aplicando evento: " + event.eventId() + " status " + event.targetStatus());
            useCase.execute(event);
        }

        System.out.println("StartupRunner finalizado");
    }
}
