package com.homihq.db2rest.jdbc.core.service;

import com.homihq.db2rest.core.dto.CreateResponse;
import com.homihq.db2rest.core.exception.GenericDataAccessException;
import com.homihq.db2rest.jdbc.JdbcManager;
import com.db2rest.jdbc.dialect.model.DbColumn;
import com.db2rest.jdbc.dialect.model.DbTable;
import com.homihq.db2rest.jdbc.core.DbOperationService;
import com.homihq.db2rest.jdbc.dto.CreateContext;
import com.homihq.db2rest.jdbc.dto.InsertableColumn;
import com.homihq.db2rest.jdbc.sql.SqlCreatorTemplate;
import com.homihq.db2rest.jdbc.tsid.TSIDProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@RequiredArgsConstructor
public class JdbcCreateService implements CreateService {

    private final TSIDProcessor tsidProcessor;
    private final SqlCreatorTemplate sqlCreatorTemplate;
    private final JdbcManager jdbcManager;
    private final DbOperationService dbOperationService;

    @Override
    public CreateResponse save(
            String dbId,
            String schemaName, String tableName, List<String> includedColumns,
            Map<String, Object> data, boolean tsIdEnabled, List<String> sequences) {
        try {
            DbTable dbTable = jdbcManager.getTable(dbId, schemaName, tableName);

            List<String> insertableColumns = buildInsertableColumns(includedColumns, data);

            Map<String, Object> tsIdMap = null;
            if (tsIdEnabled) {
                tsIdMap = processTsId(dbTable, insertableColumns, data);
            }

            List<InsertableColumn> insertableColumnList = 
                toInsertableColumnList(insertableColumns);

            processSequences(sequences, insertableColumnList, dbTable);

            this.jdbcManager.getDialect(dbId)
                .processTypes(dbTable, insertableColumns, data);

            CreateContext context = new CreateContext(
                dbId, dbTable, insertableColumns, insertableColumnList);
            String sql = sqlCreatorTemplate.create(context);

            log.debug("SQL - {}", sql);
            log.debug("Data - {}", data);

            CreateResponse createResponse = executeCreate(dbId, data, sql, dbTable);

            if (tsIdEnabled) {
                assert createResponse != null;
                if (Objects.isNull(createResponse.keys())) {
                    return new CreateResponse(createResponse.row(), tsIdMap);
                }
            }

            return createResponse;
        } catch (DataAccessException e) {
            log.error("Error", e);
            throw new GenericDataAccessException("Error insert - " + e.getMessage());
        }
    }

    private List<String> buildInsertableColumns(
            List<String> includedColumns, Map<String, Object> data) {
        return isEmpty(includedColumns)
                ? new ArrayList<>(data.keySet().stream().toList())
                : new ArrayList<>(includedColumns);
    }

    private Map<String, Object> processTsId(
            DbTable dbTable, List<String> insertableColumns, Map<String, Object> data) {
        List<DbColumn> pkColumns = dbTable.buildPkColumns();
        for (DbColumn pkColumn : pkColumns) {
            log.debug("Adding primary key columns - {}", pkColumn.name());
            insertableColumns.add(pkColumn.name());
        }
        return tsidProcessor.processTsId(data, pkColumns);
    }

    private List<InsertableColumn> toInsertableColumnList(
            List<String> insertableColumns) {
        List<InsertableColumn> insertableColumnList = new ArrayList<>();
        for (String colName : insertableColumns) {
            insertableColumnList.add(new InsertableColumn(colName, null));
        }
        return insertableColumnList;
    }

    private void processSequences(
            List<String> sequences,
            List<InsertableColumn> insertableColumnList,
            DbTable dbTable) {
        log.debug("Sequences - {}", sequences);
        if (Objects.nonNull(sequences)) {
            for (String sequence : sequences) {
                String[] colSeq = sequence.split(":");
                if (colSeq.length == 2) {
                    String columnName = colSeq[0];
                    String sequenceValue = colSeq[1];
                    if (sequenceValue.contains("fn[")) {
                        updateOrAddInsertableColumn(
                            insertableColumnList, columnName, sequenceValue);
                    } else {
                        updateOrAddInsertableColumn(
                            insertableColumnList, columnName,
                            dbTable.schema() + "." + sequenceValue + ".nextval");
                    }
                }
            }
        }
    }

    private CreateResponse executeCreate(
            String dbId, Map<String, Object> data,
            String sql, DbTable dbTable) {
        return this.jdbcManager.getTxnTemplate(dbId).execute(status -> {
            try {
                return dbOperationService.create(
                        jdbcManager.getNamedParameterJdbcTemplate(dbId),
                        data, sql, dbTable);
            } catch (Exception e) {
                status.setRollbackOnly();
                throw new GenericDataAccessException(
                    "Error insert - " + e.getMessage());
            }
        });
    }

    private void updateOrAddInsertableColumn(
            List<InsertableColumn> insertableColumnList,
            String columnName, String sequenceValue) {
        for (int i = 0; i < insertableColumnList.size(); i++) {
            InsertableColumn column = insertableColumnList.get(i);
            if (columnName.equals(column.getColumnName())) {
                insertableColumnList.set(i, 
                    new InsertableColumn(columnName, sequenceValue));
                return;
            }
        }
        insertableColumnList.add(new InsertableColumn(columnName, sequenceValue));
    }
}