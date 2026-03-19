package com.homihq.db2rest.jdbc.dto;

import com.db2rest.jdbc.dialect.model.DbTable;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public record CreateContext(
        String dbId,
        DbTable table,
        List<String> insertableColumns,
        List<InsertableColumn> insertableColumnList
) {

    private List<String> getColumnNames() {
        return insertableColumnList.stream()
                .map(c -> c.columnName)
                .toList();
    }

    private List<String> getParamNames() {
        return insertableColumnList.stream()
                .map(col -> {
                    String seq = col.sequence;

                    // Case 1: fn[...] raw SQL fragment
                    if (seq != null) {
                        String inner = FnUtil.extractFn(seq);
                        if (inner != null) {
                            // swap placeholder -> named bind for THIS column
                            String frag = FnUtil.substituteColumnPlaceholder(inner, col.columnName).trim();

                            // Optional: safety guard (tune or disable as you wish)
                            if (!FnUtil.isSafe(frag)) {
                                throw new IllegalArgumentException(
                                        "Unsafe fn[] fragment for column '" + col.columnName + "': " + frag);
                            }
                            return frag; // inline raw SQL; DB handles functions
                        }
                    }

                    // Case 2: explicit non-empty raw expression/sequence
                    if (!StringUtils.isBlank(seq)) {
                        return seq.trim();
                    }

                    // Case 3: default named bind
                    return ":" + col.columnName;
                })
                .toList();
    }

    public String renderColumns() {
        return StringUtils.join(getColumnNames(), ",");
    }

    public String renderParams() {
        return StringUtils.join(getParamNames(), ",");
    }
}
