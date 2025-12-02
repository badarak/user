package com.badarak.domain.port.in;

import com.badarak.domain.event.UserCreated;

public interface UserEventPublisher {
    void publish(UserCreated event);
}
