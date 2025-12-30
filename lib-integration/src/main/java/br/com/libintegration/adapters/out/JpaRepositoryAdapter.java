
package br.com.libintegration.adapters.out;

import br.com.libdomain.ports.RepositoryPort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/*
@Component
public class JpaRepositoryAdapter<T, ID> implements RepositoryPort<T, ID> {
    private final CrudRepository<T, ID> repository;

    public JpaRepositoryAdapter(CrudRepository<T, ID> repository) {
        this.repository = repository;
    }

    @Override
    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }

    @Override
    public void save(T entity) {
        repository.save(entity);
    }
}
*/