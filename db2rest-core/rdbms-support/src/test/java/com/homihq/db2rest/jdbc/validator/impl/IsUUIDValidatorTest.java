package com.homihq.db2rest.jdbc.validator.impl;

import com.homihq.db2rest.core.exception.PlaceholderConstraintException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

class IsUUIDValidatorTest {

    private IsUUIDValidator validator;

    @BeforeEach
    void setUp() {
        validator = new IsUUIDValidator();
    }

    @Test
    void shouldNotThrowForValidUUID() {
        assertThatNoException()
            .isThrownBy(() -> validator.validate(
                "550e8400-e29b-41d4-a716-446655440000", "id"));
    }

    @Test
    void shouldThrowForInvalidUUID() {
        assertThatThrownBy(() -> validator.validate("not-a-uuid", "id"))
            .isInstanceOf(PlaceholderConstraintException.class);
    }

    @Test
    void shouldThrowForNull() {
        assertThatThrownBy(() -> validator.validate(null, "id"))
            .isInstanceOf(PlaceholderConstraintException.class);
    }

    @Test
    void shouldThrowForNonStringValue() {
        assertThatThrownBy(() -> validator.validate(12345, "id"))
            .isInstanceOf(PlaceholderConstraintException.class);
    }

    @Test
    void shouldThrowForEmptyString() {
        assertThatThrownBy(() -> validator.validate("", "id"))
            .isInstanceOf(PlaceholderConstraintException.class);
    }
}