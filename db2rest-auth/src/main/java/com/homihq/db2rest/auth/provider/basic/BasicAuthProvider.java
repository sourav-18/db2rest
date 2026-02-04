package com.homihq.db2rest.auth.provider.basic;

import com.homihq.db2rest.auth.data.User;
import com.homihq.db2rest.auth.data.UserDetail;
import com.homihq.db2rest.auth.datalookup.AuthDataLookup;
import com.homihq.db2rest.auth.provider.AbstractAuthProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.AntPathMatcher;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;


@Slf4j
public class BasicAuthProvider extends AbstractAuthProvider {
    private static final String BASIC_AUTH = "Basic";

    public BasicAuthProvider(AuthDataLookup authDataLookup, AntPathMatcher antPathMatcher) {
        super(authDataLookup, antPathMatcher);
    }

    @Override
    public boolean canHandle(HttpServletRequest request) {
        String authHeader = this.getAuthHeader(request);
        return StringUtils.isNotBlank(authHeader) && authHeader.startsWith(BASIC_AUTH);
    }

    @Override
    public UserDetail authenticate(HttpServletRequest request) {
        String authHeader = this.getAuthHeader(request);
        if (authHeader == null || !authHeader.startsWith(BASIC_AUTH)) {
            return null;
        }

        String base64Credentials = authHeader.substring(String.format("%s ", BASIC_AUTH).length());
        byte[] decodedCredentials = Base64.getDecoder().decode(base64Credentials);
        String credentials = new String(decodedCredentials, StandardCharsets.UTF_8);

        String[] parts = credentials.split(":");
        String username = parts[0];
        String password = parts[1];


        Optional<User> user = authDataLookup.getUserByUsername(username);

        if (user.isPresent() && StringUtils.equals(password, user.get().password())) {
            return new UserDetail(username, user.get().roles());
        }

        return null;

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
