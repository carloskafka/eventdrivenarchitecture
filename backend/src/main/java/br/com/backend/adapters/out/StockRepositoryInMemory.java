package br.com.backend.adapters.out;

import br.com.backend.model.stock.Stock;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StockRepositoryInMemory implements StockRepository {

    private final Map<String, Stock> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Stock> findById(String id) {
        Stock s = store.get(id);
        return s == null ? Optional.empty() : Optional.of(s);
    }

    @Override
    public void save(Stock entity) {
        store.put(entity.getProductId(), entity);
    }
}

