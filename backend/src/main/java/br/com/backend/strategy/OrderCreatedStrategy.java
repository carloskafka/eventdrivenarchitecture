
package br.com.backend.strategy;

import br.com.libdomain.model.Event;
import br.com.libdomain.ports.RepositoryPort;
import br.com.libdomain.strategy.EventStrategy;

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
        String data = repositoryPort.findById(orderId).get().toString();

        System.out.println("Order created with data: " + data);
    }
}
