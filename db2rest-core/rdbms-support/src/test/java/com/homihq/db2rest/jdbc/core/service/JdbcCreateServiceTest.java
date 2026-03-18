package com.homihq.db2rest.jdbc.core.service;

import com.db2rest.jdbc.dialect.Dialect;
import com.db2rest.jdbc.dialect.model.DbTable;
import com.homihq.db2rest.core.dto.CreateResponse;
import com.homihq.db2rest.jdbc.JdbcManager;
import com.homihq.db2rest.jdbc.core.DbOperationService;
import com.homihq.db2rest.jdbc.sql.SqlCreatorTemplate;
import com.homihq.db2rest.jdbc.tsid.TSIDProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JdbcCreateServiceTest {

    @Mock
    private TSIDProcessor tsidProcessor;

    @Mock
    private SqlCreatorTemplate sqlCreatorTemplate;

    @Mock
    private JdbcManager jdbcManager;

    @Mock
    private DbOperationService dbOperationService;

    @Mock
    private DbTable dbTable;

    @Mock
    private Dialect dialect;

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Mock
    private TransactionTemplate transactionTemplate;

    private JdbcCreateService jdbcCreateService;

    @BeforeEach
    void setUp() {
        jdbcCreateService = new JdbcCreateService(
            tsidProcessor, sqlCreatorTemplate, jdbcManager, dbOperationService);
    }

    @Test
    void shouldBuildInsertableColumnsFromDataWhenNoColumnsSpecified() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "John");
        data.put("age", 30);

        String sql = "INSERT INTO person (name, age) VALUES (:name, :age)";
        CreateResponse expectedResponse = new CreateResponse(1, null);

        when(jdbcManager.getTable(anyString(), anyString(), anyString()))
            .thenReturn(dbTable);
        when(jdbcManager.getDialect(anyString())).thenReturn(dialect);
        when(jdbcManager.getNamedParameterJdbcTemplate(anyString()))
            .thenReturn(namedParameterJdbcTemplate);
        when(jdbcManager.getTxnTemplate(anyString()))
            .thenReturn(transactionTemplate);
        when(sqlCreatorTemplate.create(any())).thenReturn(sql);
        when(transactionTemplate.execute(any())).thenReturn(expectedResponse);
        when(dbTable.schema()).thenReturn("public");

        CreateResponse response = jdbcCreateService.save(
            "mydb", "public", "person",
            List.of(), data, false, null);

        assertThat(response).isNotNull();
        assertThat(response.row()).isEqualTo(1);
    }

    @Test
    void shouldUseProvidedColumnsWhenSpecified() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "John");
        data.put("age", 30);
        data.put("email", "john@example.com");

        List<String> includedColumns = List.of("name", "age");
        String sql = "INSERT INTO person (name, age) VALUES (:name, :age)";
        CreateResponse expectedResponse = new CreateResponse(1, null);

        when(jdbcManager.getTable(anyString(), anyString(), anyString()))
            .thenReturn(dbTable);
        when(jdbcManager.getDialect(anyString())).thenReturn(dialect);
        when(jdbcManager.getNamedParameterJdbcTemplate(anyString()))
            .thenReturn(namedParameterJdbcTemplate);
        when(jdbcManager.getTxnTemplate(anyString()))
            .thenReturn(transactionTemplate);
        when(sqlCreatorTemplate.create(any())).thenReturn(sql);
        when(transactionTemplate.execute(any())).thenReturn(expectedResponse);
        when(dbTable.schema()).thenReturn("public");

        CreateResponse response = jdbcCreateService.save(
            "mydb", "public", "person",
            includedColumns, data, false, null);

        assertThat(response).isNotNull();
        assertThat(response.row()).isEqualTo(1);
    }
}