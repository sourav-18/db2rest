package com.homihq.db2rest.jdbc.util;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AliasGeneratorTest {

    @Test
    void shouldTruncateIdentifierLongerThanPrefixLength() {
        String longIdentifier = "customer_orders";
        String alias = AliasGenerator.getAlias(longIdentifier);

        assertThat(alias).startsWith("cust_");
        assertThat(alias.length()).isGreaterThan(5);
    }

    @Test
    void shouldKeepIdentifierShorterThanPrefixLength() {
        String shortIdentifier = "id";
        String alias = AliasGenerator.getAlias(shortIdentifier);

        assertThat(alias).startsWith("id_");
    }

    @Test
    void shouldKeepIdentifierEqualToPrefixLength() {
        String exactIdentifier = "name";
        String alias = AliasGenerator.getAlias(exactIdentifier);

        assertThat(alias).startsWith("name_");
    }

    @Test
    void shouldGenerateUniqueAliasesForSameIdentifier() {
        String identifier = "orders";
        String alias1 = AliasGenerator.getAlias(identifier);
        String alias2 = AliasGenerator.getAlias(identifier);

        assertThat(alias1).isNotEqualTo(alias2);
    }

    @Test
    void shouldAlwaysContainUnderseparator() {
        String identifier = "products";
        String alias = AliasGenerator.getAlias(identifier);

        assertThat(alias).contains("_");
    }
}