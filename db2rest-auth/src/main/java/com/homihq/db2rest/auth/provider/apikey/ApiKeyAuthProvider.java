package com.homihq.db2rest.auth.provider.apikey;

import com.homihq.db2rest.auth.data.ApiKey;
import com.homihq.db2rest.auth.data.UserDetail;
import com.homihq.db2rest.auth.datalookup.AuthDataLookup;
import com.homihq.db2rest.auth.provider.AbstractAuthProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.AntPathMatcher;

public class ApiKeyAuthProvider extends AbstractAuthProvider {
    private static final String API_KEY_HEADER = "X-API-KEY";

    public ApiKeyAuthProvider(AuthDataLookup authDataLookup, AntPathMatcher antPathMatcher) {
        super(authDataLookup, antPathMatcher);
    }

    @Override
    public boolean canHandle(HttpServletRequest request) {
        String apiKey = request.getHeader(API_KEY_HEADER);
        return StringUtils.isNotBlank(apiKey);
    }

    @Override
    public UserDetail authenticate(HttpServletRequest request) {
        String apiKey = request.getHeader(API_KEY_HEADER);
        return authDataLookup.getApiKeys()
                .stream()
                .filter(a -> a.key().equals(apiKey))
                .filter(ApiKey::active)
                .map(a -> new UserDetail(a.key(), a.roles()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean authorize(UserDetail userDetail, String requestUri, String method) {
        return this.authorizeInternal(userDetail, requestUri, method, authDataLookup.getApiResourceRoles(), antPathMatcher);
    }

    @Override
    public boolean isExcluded(String requestUri, String method) {
        return super.isExcludedInternal(requestUri, method, authDataLookup.getExcludedResources(), antPathMatcher);
    }
}
