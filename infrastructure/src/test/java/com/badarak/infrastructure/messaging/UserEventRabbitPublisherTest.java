package com.badarak.infrastructure.messaging;

import com.badarak.domain.event.DomainEvent;
import com.badarak.domain.event.UserCreatedEvent;
import com.badarak.domain.model.Email;
import com.badarak.domain.model.UserId;
import com.badarak.domain.model.UserName;
import com.badarak.domain.port.out.DomainEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.stream.function.StreamBridge;

import java.util.UUID;

import static com.badarak.infrastructure.messaging.UserEventRabbitPublisher.USER_CREATED_OUT;
import static org.mockito.Mockito.*;

class UserEventRabbitPublisherTest {
    private final StreamBridge streamBridge = mock(StreamBridge.class);
    private final DomainEventPublisher userEventRabbitPublisher = new UserEventRabbitPublisher(streamBridge);


    @Test
    void should_publish_user_created_event() {
        //Given
        final var email = new Email("john.doe@example.com");
        final var name = new UserName("John", "Doe");
        UserId userId = new UserId(UUID.randomUUID());
        DomainEvent event = new UserCreatedEvent(userId, email, name);

        //When
        userEventRabbitPublisher.publish(event);

        // Then
        verify(streamBridge, times(1)).send(USER_CREATED_OUT, event);
    }
}