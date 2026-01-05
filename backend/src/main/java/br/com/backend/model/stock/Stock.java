package br.com.backend.model.stock;

public class Stock {
    private final String productId;
    private int quantity;

    public Stock(String productId, int initialQuantity) {
        this.productId = productId;
        this.quantity = initialQuantity;
    }

    public synchronized boolean reserve(int qty) {
        if (qty <= 0) return false;
        if (quantity < qty) return false;
        quantity -= qty;
        return true;
    }

    public synchronized void release(int qty) {
        if (qty <= 0) return;
        quantity += qty;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}

