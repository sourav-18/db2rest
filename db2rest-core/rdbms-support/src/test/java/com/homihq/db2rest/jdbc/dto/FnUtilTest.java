package com.homihq.db2rest.jdbc.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class FnUtilTest {

    @Test
    void shouldExtractContentFromFnBlock() {
        String result = FnUtil.extractFn("fn[NOW()]");
        assertThat(result).isEqualTo("NOW()");
    }

    @Test
    void shouldReturnNullWhenNoFnBlock() {
        String result = FnUtil.extractFn("plain_value");
        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullForNullInput() {
        String result = FnUtil.extractFn(null);
        assertThat(result).isNull();
    }

    @Test
    void shouldHandleNestedBrackets() {
        String result = FnUtil.extractFn("fn[COALESCE(col, 'default')]");
        assertThat(result).isEqualTo("COALESCE(col, 'default')");
    }

    @Test
    void shouldSubstituteColumnPlaceholder() {
        String result = FnUtil.substituteColumnPlaceholder(
            "UPPER(<column_name>)", "username");
        assertThat(result).isEqualTo("UPPER(:username)");
    }

    @Test
    void shouldSubstituteUpperCasePlaceholder() {
        String result = FnUtil.substituteColumnPlaceholder(
            "UPPER(<COLUMN_NAME>)", "username");
        assertThat(result).isEqualTo("UPPER(:username)");
    }

    @Test
    void shouldSubstituteDollarPlaceholder() {
        String result = FnUtil.substituteColumnPlaceholder(
            "UPPER(${column_name})", "username");
        assertThat(result).isEqualTo("UPPER(:username)");
    }

    @Test
    void shouldReturnNullForNullSubstitution() {
        String result = FnUtil.substituteColumnPlaceholder(null, "username");
        assertThat(result).isNull();
    }

    @Test
    void shouldReturnTrueForSafeFragment() {
        assertThat(FnUtil.isSafe("UPPER(:username)")).isTrue();
        assertThat(FnUtil.isSafe("NOW()")).isTrue();
        assertThat(FnUtil.isSafe(null)).isTrue();
    }

    @Test
    void shouldReturnFalseForUnsafeFragmentWithDrop() {
        assertThat(FnUtil.isSafe("SELECT 1; DROP TABLE users")).isFalse();
    }

    @Test
    void shouldReturnFalseForUnsafeFragmentWithComment() {
        assertThat(FnUtil.isSafe("value -- comment")).isFalse();
    }

    @Test
    void shouldReturnFalseForUnsafeFragmentWithAlter() {
        assertThat(FnUtil.isSafe("value; ALTER TABLE users")).isFalse();
    }
}