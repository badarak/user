package com.badarak.domain.port.out;

import com.badarak.domain.event.DomainEvent;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
