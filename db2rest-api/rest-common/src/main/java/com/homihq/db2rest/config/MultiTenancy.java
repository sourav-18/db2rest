package com.homihq.db2rest.config;

import java.util.List;
import java.util.Map;

import com.homihq.db2rest.auth.data.RoleDataFilter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MultiTenancy {
    public static final String ROLEBASEDDATAFILTERS = "roleBasedDataFilters";

    public static String joinFilters(String filter, String dbId, String table, List<RoleDataFilter> roleBasedDataFilters) {
        StringBuilder filterBuilder = new StringBuilder(filter);
        if (roleBasedDataFilters != null) {
            for (RoleDataFilter roleDataFilter : roleBasedDataFilters) {
                if (dbId.equalsIgnoreCase(roleDataFilter.dbId()) && table.equalsIgnoreCase(roleDataFilter.table())) {
                    if (!filterBuilder.isEmpty()) filterBuilder.append(";");
                    filterBuilder.append(roleDataFilter.column());
                    filterBuilder.append("==");
                    filterBuilder.append(roleDataFilter.value());
                }
            }
        }
        return filterBuilder.toString();
    }

    public static void addTenantColumns(List<Map<String, Object>> data, String dbId, String table, List<RoleDataFilter> roleBasedDataFilters) {
        for(Map<String, Object> dataItem : data) {
            addTenantColumns(dataItem, dbId, table, roleBasedDataFilters);
        }
    }

    public static void addTenantColumns(Map<String, Object> data, String dbId, String table, List<RoleDataFilter> roleBasedDataFilters) {
        if (roleBasedDataFilters != null) {
            for (RoleDataFilter roleDataFilter : roleBasedDataFilters) {
                if (dbId.equalsIgnoreCase(roleDataFilter.dbId()) && table.equalsIgnoreCase(roleDataFilter.table())) {
                    data.put(roleDataFilter.column(), roleDataFilter.value());
                }
            }
        }
    }
}
