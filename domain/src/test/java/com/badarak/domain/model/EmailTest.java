package com.badarak.domain.model;

import com.badarak.domain.exception.InvalidEmailException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class EmailTest {

    @Test
    void should_accept_valid_formats() {
        assertThatCode(() -> new Email("john.doe@example.com"))
                .doesNotThrowAnyException();
    }

    @Test
    void should_normalize_to_lowercase() {
        assertThat(new Email("JOHN@EXAMPLE.COM").value())
                .isEqualTo("john@example.com");
    }

    @ParameterizedTest
    @ValueSource(strings = {"notanemail", "missing@", "@nodomain.com", ""})
    void should_reject_invalid_format(String invalid) {
        assertThatThrownBy(() -> new Email(invalid))
                .isInstanceOf(InvalidEmailException.class);
    }

    @Test
    @DisplayName("rejects null")
    void should_reject_null() {
        assertThatNullPointerException()
                .isThrownBy(() -> new Email(null));
    }

}