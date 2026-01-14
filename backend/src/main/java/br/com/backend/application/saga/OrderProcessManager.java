package br.com.backend.application.saga;

import br.com.backend.application.ports.out.OrderRepository;
import br.com.backend.domain.payment.events.PaymentApprovedDomainEvent;
import br.com.backend.domain.order.Order;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OrderProcessManager {

    private final OrderRepository orderRepository;

    public OrderProcessManager(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @EventListener
    public void on(PaymentApprovedDomainEvent event) {
        System.out.println("[Saga] Recebido PaymentApprovedDomainEvent para order " + event.orderId());

        // ⚠️ Certifique-se de que a Order existe
        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new RuntimeException("Order não encontrada: " + event.orderId()));

        order.markAsPaid();
        orderRepository.save(order);

        System.out.println("[Saga] Order " + order.getOrderId() + " atualizado para PAID");
    }

}
