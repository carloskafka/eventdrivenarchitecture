package br.com.backend.application.ports.out;

import br.com.backend.domain.order.Order;

import java.util.Optional;

public interface OrderRepository {
    Optional<Order> findById(String orderId);
    void save(Order order);
}
