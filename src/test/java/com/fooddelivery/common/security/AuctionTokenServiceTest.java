package com.fooddelivery.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuctionTokenServiceTest {

    private AuctionTokenService auctionTokenService;
    private final String SECRET_KEY = "test-secret-key-for-auction-token-aes-gcm-32-bytes";

    @BeforeEach
    void setUp() {
        auctionTokenService = new AuctionTokenService(SECRET_KEY, new org.springframework.mock.env.MockEnvironment());
    }

    @Test
    void testValidToken() {
        UUID campaignId = UUID.randomUUID();
        UUID advertiserId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("1.50");
        UUID auctionId = UUID.randomUUID();
        
        String tokenStr = auctionTokenService.issue(campaignId, advertiserId, price, auctionId, Duration.ofMinutes(5));
        
        AuctionTokenService.AuctionToken token = auctionTokenService.verify(tokenStr);
        
        assertEquals(campaignId, token.campaignId());
        assertEquals(advertiserId, token.advertiserId());
        assertEquals(price, token.getPriceAsBigDecimal());
        assertEquals(auctionId, token.auctionId());
    }

    @Test
    void testTamperedCiphertext() {
        UUID campaignId = UUID.randomUUID();
        UUID advertiserId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("1.50");
        UUID auctionId = UUID.randomUUID();
        
        String tokenStr = auctionTokenService.issue(campaignId, advertiserId, price, auctionId, Duration.ofMinutes(5));
        
        // Decode token, flip a bit in ciphertext, and re-encode
        byte[] raw = Base64.getUrlDecoder().decode(tokenStr);
        raw[20] ^= 1; // Flip a bit in the ciphertext
        String tamperedTokenStr = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            auctionTokenService.verify(tamperedTokenStr);
        });
        assertTrue(exception.getMessage().contains("Token verification failed"));
    }

    @Test
    void testExpiredToken() throws InterruptedException {
        UUID campaignId = UUID.randomUUID();
        UUID advertiserId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("1.50");
        UUID auctionId = UUID.randomUUID();
        
        // Issue token with 10ms TTL
        String tokenStr = auctionTokenService.issue(campaignId, advertiserId, price, auctionId, Duration.ofMillis(10));
        
        Thread.sleep(20);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            auctionTokenService.verify(tokenStr);
        });
        assertTrue(exception.getMessage().contains("Token expired"));
    }

    /**
     * The value Deployment/application.yml falls back to when AUCTION_TOKEN_SECRET is unset.
     * Read from the deployed config, not from DEV_SECRET -- the whole point is that the two
     * must agree for the prod guard to fire.
     */
    private static final String DEPLOYED_DEFAULT =
            "dev-only-insecure-auction-secret-override-in-production";

    private static org.springframework.core.env.Environment prodEnv() {
        org.springframework.mock.env.MockEnvironment env = new org.springframework.mock.env.MockEnvironment();
        env.setActiveProfiles("prod");
        return env;
    }

    @Test
    void prodRefusesTheDeployedFallbackSecret() {
        assertThrows(IllegalStateException.class,
                () -> new AuctionTokenService(DEPLOYED_DEFAULT, prodEnv()),
                "prod booted with the repo-committed auction key from Deployment/application.yml");
    }

    @Test
    void prodRefusesTheDevSecretConstant() {
        assertThrows(IllegalStateException.class,
                () -> new AuctionTokenService(AuctionTokenService.DEV_SECRET, prodEnv()));
    }

    @Test
    void prodRefusesABlankSecret() {
        assertThrows(IllegalStateException.class,
                () -> new AuctionTokenService("", prodEnv()));
    }

    @Test
    void prodAcceptsARealSecret() {
        assertDoesNotThrow(() -> new AuctionTokenService(SECRET_KEY, prodEnv()));
    }
}
