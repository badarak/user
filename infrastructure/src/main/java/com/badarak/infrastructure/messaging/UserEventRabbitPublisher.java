package com.badarak.infrastructure.messaging;

import com.badarak.domain.event.DomainEvent;
import com.badarak.domain.port.out.DomainEventPublisher;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
public class UserEventRabbitPublisher implements DomainEventPublisher {
    private final StreamBridge streamBridge;
    public static final String USER_CREATED_OUT = "userCreatedOutPut";


    public UserEventRabbitPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Override
    public void publish(DomainEvent event) {
        streamBridge.send(USER_CREATED_OUT, event);
    }
}
