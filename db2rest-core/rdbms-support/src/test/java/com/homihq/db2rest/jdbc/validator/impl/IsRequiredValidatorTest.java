package com.homihq.db2rest.jdbc.validator.impl;

import com.homihq.db2rest.core.exception.PlaceholderConstraintException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

class IsRequiredValidatorTest {

    private IsRequiredValidator validator;

    @BeforeEach
    void setUp() {
        validator = new IsRequiredValidator();
    }

    @Test
    void shouldThrowExceptionWhenValueIsNull() {
        assertThatThrownBy(() -> validator.validate(null, "username"))
            .isInstanceOf(PlaceholderConstraintException.class);
    }

    @Test
    void shouldThrowExceptionWhenValueIsEmptyString() {
        assertThatThrownBy(() -> validator.validate("", "username"))
            .isInstanceOf(PlaceholderConstraintException.class);
    }

    @Test
    void shouldThrowExceptionWhenValueIsBlankString() {
        assertThatThrownBy(() -> validator.validate("   ", "username"))
            .isInstanceOf(PlaceholderConstraintException.class);
    }

    @Test
    void shouldNotThrowExceptionWhenValueIsValidString() {
        assertThatNoException()
            .isThrownBy(() -> validator.validate("john", "username"));
    }

    @Test
    void shouldNotThrowExceptionWhenValueIsNumber() {
        assertThatNoException()
            .isThrownBy(() -> validator.validate(42, "age"));
    }

    @Test
    void shouldNotThrowExceptionWhenValueIsObject() {
        assertThatNoException()
            .isThrownBy(() -> validator.validate(new Object(), "data"));
    }
}