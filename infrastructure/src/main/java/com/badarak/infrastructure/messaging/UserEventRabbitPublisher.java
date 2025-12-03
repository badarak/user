package com.badarak.infrastructure.messaging;

import com.badarak.domain.event.UserCreated;
import com.badarak.domain.port.in.UserEventPublisher;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Component
public class UserEventRabbitPublisher implements UserEventPublisher {
    private final StreamBridge streamBridge;
    public static final String USER_CREATED_OUT = "userCreatedOutPut";


    public UserEventRabbitPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Override
    public void publish(UserCreated event) {
        streamBridge.send(USER_CREATED_OUT, event);
    }
}
