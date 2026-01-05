package br.com.backend.adapters.out;

import br.com.backend.model.stock.Stock;
import br.com.libdomain.ports.RepositoryPort;

public interface StockRepository extends RepositoryPort<Stock, String> {
}

