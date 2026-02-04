package com.homihq.db2rest.auth.data;

public record RoleDataFilter(String role, String dbId, String table, String column, Object value) {
}
