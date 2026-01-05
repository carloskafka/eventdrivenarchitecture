package br.com.backend.model.order;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Order {
    private final String orderId;
    private OrderStatus status;
    private final Map<String, Integer> items = new HashMap<>(); // productId -> qty
    private final long version;

    public Order(String orderId) {
        this.orderId = orderId;
        this.status = OrderStatus.NEW;
        this.version = 0L;
    }

    public boolean addItem(String productId, int qty) {
        if (status != OrderStatus.NEW) return false;
        items.merge(productId, qty, Integer::sum);
        return true;
    }

    public boolean applyStatus(OrderStatus target) {
        if (!status.canTransitionTo(target)) return false;
        this.status = target;
        return true;
    }

    public String getOrderId() {
        return orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Map<String, Integer> getItems() {
        return new HashMap<>(items);
    }
}

