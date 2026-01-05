package br.com.backend.adapters.out;

import br.com.backend.model.order.Order;
import br.com.libdomain.ports.RepositoryPort;

public interface OrderRepository extends RepositoryPort<Order, String> {
}

