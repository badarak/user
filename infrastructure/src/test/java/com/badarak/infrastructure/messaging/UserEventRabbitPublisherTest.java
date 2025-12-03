package com.badarak.infrastructure.messaging;

import com.badarak.domain.event.UserCreated;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.stream.function.StreamBridge;

import java.util.UUID;

import static com.badarak.infrastructure.messaging.UserEventRabbitPublisher.USER_CREATED_OUT;
import static org.mockito.Mockito.*;

class UserEventRabbitPublisherTest {
    private final StreamBridge streamBridge = mock(StreamBridge.class);
    private final UserEventRabbitPublisher userEventRabbitPublisher = new UserEventRabbitPublisher(streamBridge);


    @Test
    void should_publish_user_created_event() {
        //Given
        UserCreated event = new UserCreated(UUID.randomUUID(), "test@gmail.com");

        //When
        userEventRabbitPublisher.publish(event);

        // Then
        verify(streamBridge, times(1)).send(USER_CREATED_OUT, event);
    }
}