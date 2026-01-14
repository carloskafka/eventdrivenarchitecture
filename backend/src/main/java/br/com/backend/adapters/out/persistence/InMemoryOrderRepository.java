package br.com.backend.adapters.out.persistence;

import br.com.backend.application.ports.out.OrderRepository;
import br.com.backend.domain.order.Order;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<String, Order> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Order> findById(String orderId) {
        return Optional.ofNullable(store.get(orderId));
    }

    @Override
    public void save(Order order) {
        store.put(order.getOrderId(), order);
    }
}
