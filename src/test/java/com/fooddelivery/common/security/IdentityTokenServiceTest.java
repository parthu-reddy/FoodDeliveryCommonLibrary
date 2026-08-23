package com.fooddelivery.common.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IdentityTokenServiceTest {

    private final IdentityTokenService identityTokenService = new IdentityTokenService("my-super-secret-key-that-is-long-enough-to-be-secure", new org.springframework.mock.env.MockEnvironment());

    @Test
    void shouldGenerateAndVerifyValidSignature() {
        String userId = "user-123";
        String roles = "ROLE_USER";
        String phone = "1234567890";
        String sessionId = "session-1";
        long issuedAt = 1692698400000L;

        String signature = identityTokenService.sign(userId, roles, phone, sessionId, issuedAt);
        assertNotNull(signature);
        
        boolean isValid = identityTokenService.verify(signature, userId, roles, phone, sessionId, issuedAt);
        assertTrue(isValid);
    }

    @Test
    void shouldRejectTamperedSignature() {
        String userId = "user-123";
        String roles = "ROLE_USER";
        String phone = "1234567890";
        String sessionId = "session-1";
        long issuedAt = 1692698400000L;

        String signature = identityTokenService.sign(userId, roles, phone, sessionId, issuedAt);
        assertNotNull(signature);
        
        boolean isValid = identityTokenService.verify(signature, userId, "ROLE_ADMIN", phone, sessionId, issuedAt);
        assertFalse(isValid);
    }
}
