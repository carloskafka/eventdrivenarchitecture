package br.com.backend.strategy;

import br.com.libdomain.model.Event;
import br.com.libdomain.ports.RepositoryPort;
import br.com.libdomain.strategy.EventStrategy;

import java.util.Optional;

public class OrderCreatedStrategy implements EventStrategy {

    private final RepositoryPort repositoryPort;

    public OrderCreatedStrategy(RepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public boolean supports(Event event) {
        return "ORDER_CREATED".equals(event.type());
    }

    @Override
    public void execute(Event event) {
        String orderId = (String) event.payload().get("orderId");

        Optional order = repositoryPort.findById(orderId);

        if (order.isPresent()) {
            String data = order.get().toString();
            System.out.println("Order created with data: " + data);
        } else {
            System.out.println("Order not found for: " + event);
        }
    }
}
