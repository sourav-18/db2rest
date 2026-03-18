package com.homihq.db2rest.jdbc.dto;

public final class FnUtil {

    private FnUtil() {}

    /**
     * Extract the content of the first top-level fn[ ... ] block.
     * Returns null if the input is not in fn[...] form.
     * Robust to nested brackets and quoted strings.
     */
    public static String extractFn(String text) {
        if (text == null) return null;
        int i = text.indexOf("fn[");
        if (i < 0) return null;

        int start = i + 3;
        int depth = 1;
        boolean inSingle = false, inDouble = false;

        for (int pos = start; pos < text.length(); pos++) {
            char c = text.charAt(pos);

            if (c == '\'' && !inDouble && (pos == 0 || text.charAt(pos - 1) != '\\')) {
                inSingle = !inSingle;
                continue;
            }
            if (c == '"' && !inSingle && (pos == 0 || text.charAt(pos - 1) != '\\')) {
                inDouble = !inDouble;
                continue;
            }

            if (inSingle || inDouble) continue;

            if (c == '[') depth++;
            else if (c == ']') {
                depth--;
                if (depth == 0) {
                    return text.substring(start, pos);
                }
            }
        }
        return null;
    }

    /**
     * Replace common placeholders for the current column with its named bind.
     */
    public static String substituteColumnPlaceholder(String fragment, String columnName) {
        if (fragment == null) return null;
        String bind = ":" + columnName;
        return fragment
                .replace("<column_name>", bind)
                .replace("<COLUMN_NAME>", bind)
                .replace("${column_name}", bind);
    }

    /**
     * Ultra-light safety filter.
     * Blocks obvious multi-statement or DDL/DCL attempts.
     */
    public static boolean isSafe(String fragment) {
        if (fragment == null) return true;
        String s = fragment.toUpperCase();
        return !(s.contains("--")
                || s.contains("/*") || s.contains("*/")
                || s.contains(";")
                || s.contains(" EXEC ")
                || s.contains(" CALL ")
                || s.contains(" DROP ")
                || s.contains(" ALTER ")
                || s.contains(" CREATE ")
                || s.contains(" GRANT ")
                || s.contains(" REVOKE "));
    }
}