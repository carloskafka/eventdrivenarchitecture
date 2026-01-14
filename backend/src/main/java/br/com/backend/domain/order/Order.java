package br.com.backend.domain.order;

public class Order {

    private final String orderId;
    private OrderStatus status;

    public Order(String orderId) {
        this.orderId = orderId;
        this.status = OrderStatus.CREATED;
    }

    public void markAsPaid() {
        if (status != OrderStatus.CREATED) {
            return;
        }
        this.status = OrderStatus.PAID;
    }

    public String getOrderId() {
        return orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
