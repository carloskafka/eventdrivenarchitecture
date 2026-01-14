package br.com.backend.application.ports.out;

import br.com.backend.domain.shared.DomainEvent;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
