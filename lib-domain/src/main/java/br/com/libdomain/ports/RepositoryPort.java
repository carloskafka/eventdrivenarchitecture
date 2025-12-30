
package br.com.libdomain.ports;

import java.util.Optional;

/**
 * Port genérico de repositório.
 * Define operações básicas de persistência para qualquer agregado.
 * Não conhece nenhum conceito específico de domínio.
 *
 * @param <T>  Tipo do agregado
 * @param <ID> Tipo do identificador do agregado
 */
public interface RepositoryPort<T, ID> {

    /**
     * Recupera uma entidade pelo seu ID.
     *
     * @param id identificador
     * @return entidade ou Optional.empty() se não existir
     */
    Optional<T> findById(ID id);

    /**
     * Persiste ou atualiza a entidade.
     *
     * @param entity entidade a salvar
     */
    void save(T entity);

    /**
     * Opcional: remove a entidade pelo ID.
     *
     * @param id identificador
     */
    default void deleteById(ID id) {
        throw new UnsupportedOperationException("deleteById not implemented");
    }
}

