package com.homihq.db2rest.auth.data;

import java.util.List;

public record ApiResource(String path, String method, List<String> roles) {
}
