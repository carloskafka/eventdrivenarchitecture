package br.com.backend.adapters.out;

import br.com.backend.model.order.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderRepositoryInMemory implements OrderRepository {

    private final Map<String, Order> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Order> findById(String id) {
        Order o = store.get(id);
        return o == null ? Optional.empty() : Optional.of(o);
    }

    @Override
    public void save(Order entity) {
        store.put(entity.getOrderId(), entity);
    }
}

