package com.fooddelivery.common.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * Puts a SERVICE identity on outbound Feign calls regardless of current context.
 * Used for internal clients that must perform privileged operations (e.g. WalletInternalClient).
 */
public class ServiceIdentityRequestInterceptor implements RequestInterceptor {

    private final IdentityTokenService identityTokenService;
    private final String applicationName;
    public static final String SERVICE_ROLE = "SERVICE";

    public ServiceIdentityRequestInterceptor(IdentityTokenService identityTokenService, String applicationName) {
        this.identityTokenService = identityTokenService;
        this.applicationName = applicationName;
    }

    @Override
    public void apply(RequestTemplate template) {
        long issuedAt = System.currentTimeMillis();
        String signature = identityTokenService.sign(applicationName, SERVICE_ROLE, null, null, issuedAt);
        template.header(com.fooddelivery.common.constants.HeaderConstants.HEADER_USER_ID, applicationName);
        template.header(com.fooddelivery.common.constants.HeaderConstants.HEADER_USER_ROLES, SERVICE_ROLE);
        template.header(com.fooddelivery.common.constants.HeaderConstants.HEADER_IDENTITY_SIGNATURE, signature);
        template.header(com.fooddelivery.common.constants.HeaderConstants.HEADER_ISSUED_AT, String.valueOf(issuedAt));
    }
}
