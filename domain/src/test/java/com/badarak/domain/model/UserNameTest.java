package com.badarak.domain.model;

import com.badarak.domain.exception.InvalidUserNameException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.badarak.domain.exception.ErrorCode.USER_INVALID_NAME;
import static org.assertj.core.api.Assertions.*;

class UserNameTest {
    @Test
    @DisplayName("should_create_username_when_firstname_and_lastname_are_valid")
    void should_create_username_when_firstname_and_lastname_are_valid() {
        final var name = new UserName("John", "Doe");

        assertThat(name.firstName()).isEqualTo("John");
        assertThat(name.lastName()).isEqualTo("Doe");
    }

    @Test
    @DisplayName("should_strip_leading_and_trailing_whitespace_from_firstname")
    void should_strip_leading_and_trailing_whitespace_from_firstname() {
        final var name = new UserName("  John  ", "Doe");

        assertThat(name.firstName()).isEqualTo("John");
    }

    @Test
    @DisplayName("should_strip_leading_and_trailing_whitespace_from_lastname")
    void should_strip_leading_and_trailing_whitespace_from_lastname() {
        final var name = new UserName("John", "  Doe  ");

        assertThat(name.lastName()).isEqualTo("Doe");
    }

    @Test
    @DisplayName("should_strip_whitespace_from_both_fields_simultaneously")
    void should_strip_whitespace_from_both_fields_simultaneously() {
        final var name = new UserName("  Jane  ", "  Smith  ");

        assertThat(name.firstName()).isEqualTo("Jane");
        assertThat(name.lastName()).isEqualTo("Smith");
    }

    @Test
    @DisplayName("should_accept_firstname_of_exactly_100_characters")
    void should_accept_firstname_of_exactly_100_characters() {
        final var exactly100 = "A".repeat(100);

        assertThatNoException().isThrownBy(() -> new UserName(exactly100, "Doe"));
    }

    @Test
    @DisplayName("should_accept_lastname_of_exactly_100_characters")
    void should_accept_lastname_of_exactly_100_characters() {
        final var exactly100 = "Z".repeat(100);

        assertThatNoException().isThrownBy(() -> new UserName("John", exactly100));
    }

    @Test
    @DisplayName("should_accept_single_character_firstname")
    void should_accept_single_character_firstname() {
        final var name = new UserName("A", "Doe");

        assertThat(name.firstName()).isEqualTo("A");
    }

    @Test
    @DisplayName("should_accept_single_character_lastname")
    void should_accept_single_character_lastname() {
        final var name = new UserName("John", "D");

        assertThat(name.lastName()).isEqualTo("D");
    }

    @Test
    @DisplayName("should_accept_names_with_hyphens")
    void should_accept_names_with_hyphens() {
        assertThatNoException()
                .isThrownBy(() -> new UserName("Jean-Pierre", "Dupont-Martin"));
    }

    @Test
    @DisplayName("should_accept_names_with_unicode_characters")
    void should_accept_names_with_unicode_characters() {
        assertThatNoException()
                .isThrownBy(() -> new UserName("Ézéchiel", "Müller"));
    }

    @Test
    @DisplayName("should_accept_names_with_apostrophes")
    void should_accept_names_with_apostrophes() {
        assertThatNoException()
                .isThrownBy(() -> new UserName("O'Brien", "D'Artagnan"));
    }

    @Test
    @DisplayName("should_throw_InvalidUserNameException_when_firstname_is_null")
    void should_throw_InvalidUserNameException_when_firstname_is_null() {
        assertThatNullPointerException()
                .isThrownBy(() -> new UserName(null, "Doe"))
                .withMessage("firstName must not be null");
    }

    @Test
    @DisplayName("should_throw_InvalidUserNameException_when_firstname_is_blank")
    void should_throw_InvalidUserNameException_when_firstname_is_blank() {
        assertThatThrownBy(() -> new UserName("   ", "Doe"))
                .isInstanceOf(InvalidUserNameException.class)
                .hasMessageContaining("firstName");
    }

    @Test
    @DisplayName("should_throw_InvalidUserNameException_when_firstname_is_empty_string")
    void should_throw_InvalidUserNameException_when_firstname_is_empty_string() {
        assertThatThrownBy(() -> new UserName("", "Doe"))
                .isInstanceOf(InvalidUserNameException.class)
                .hasMessageContaining("firstName");
    }

    @Test
    @DisplayName("should_throw_InvalidUserNameException_when_firstname_exceeds_100_characters")
    void should_throw_InvalidUserNameException_when_firstname_exceeds_100_characters() {
        final var tooLong = "A".repeat(101);

        assertThatThrownBy(() -> new UserName(tooLong, "Doe"))
                .isInstanceOf(InvalidUserNameException.class)
                .hasMessageContaining("firstName");
    }

    @ParameterizedTest(name = "blank value: \"{0}\"")
    @DisplayName("should_throw_InvalidUserNameException_for_all_whitespace_only_firstnames")
    @ValueSource(strings = {" ", "  ", "\t", "\n", "\r\n", "  \t  "})
    void should_throw_InvalidUserNameException_for_all_whitespace_only_firstnames(String blank) {
        assertThatThrownBy(() -> new UserName(blank, "Doe"))
                .isInstanceOf(InvalidUserNameException.class);
    }

    @Test
    @DisplayName("should_include_errorCode_USER_INVALID_NAME_when_firstname_is_invalid")
    void should_include_errorCode_USER_INVALID_NAME_when_firstname_is_invalid() {
        assertThatThrownBy(() -> new UserName("", "Doe"))
                .isInstanceOf(InvalidUserNameException.class)
                .extracting("errorCode")
                .isEqualTo(USER_INVALID_NAME.name());
    }

    @Test
    @DisplayName("should_throw_NullPointerException_when_lastname_is_null")
    void should_throw_NullPointerException_when_lastname_is_null() {
        assertThatNullPointerException()
                .isThrownBy(() -> new UserName("John", null))
                .withMessage("lastName must not be null");
    }

    @Test
    @DisplayName("should_throw_InvalidUserNameException_when_lastname_is_blank")
    void should_throw_InvalidUserNameException_when_lastname_is_blank() {
        assertThatThrownBy(() -> new UserName("John", "   "))
                .isInstanceOf(InvalidUserNameException.class)
                .hasMessageContaining("lastName");
    }

    @Test
    @DisplayName("should_throw_InvalidUserNameException_when_lastname_is_empty_string")
    void should_throw_InvalidUserNameException_when_lastname_is_empty_string() {
        assertThatThrownBy(() -> new UserName("John", ""))
                .isInstanceOf(InvalidUserNameException.class)
                .hasMessageContaining("lastName");
    }

    @Test
    @DisplayName("should_throw_InvalidUserNameException_when_lastname_exceeds_100_characters")
    void should_throw_InvalidUserNameException_when_lastname_exceeds_100_characters() {
        final var tooLong = "Z".repeat(101);

        assertThatThrownBy(() -> new UserName("John", tooLong))
                .isInstanceOf(InvalidUserNameException.class)
                .hasMessageContaining("lastName");
    }

    @ParameterizedTest(name = "blank value: \"{0}\"")
    @DisplayName("should_throw_InvalidUserNameException_for_all_whitespace_only_lastnames")
    @ValueSource(strings = {" ", "  ", "\t", "\n", "\r\n"})
    void should_throw_InvalidUserNameException_for_all_whitespace_only_lastnames(String blank) {
        assertThatThrownBy(() -> new UserName("John", blank))
                .isInstanceOf(InvalidUserNameException.class);
    }

    @Test
    @DisplayName("should_include_errorCode_USER_INVALID_NAME_when_lastname_is_invalid")
    void should_include_errorCode_USER_INVALID_NAME_when_lastname_is_invalid() {
        assertThatThrownBy(() -> new UserName("John", ""))
                .isInstanceOf(InvalidUserNameException.class)
                .extracting("errorCode")
                .isEqualTo(USER_INVALID_NAME.name());
    }

    @Test
    @DisplayName("should_return_firstname_and_lastname_separated_by_space")
    void should_return_firstname_and_lastname_separated_by_space() {
        final var name = new UserName("John", "Doe");

        assertThat(name.fullName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("should_reflect_stripped_values_in_fullName")
    void should_reflect_stripped_values_in_fullName() {
        final var name = new UserName("  Jane  ", "  Smith  ");

        assertThat(name.fullName()).isEqualTo("Jane Smith");
    }
}