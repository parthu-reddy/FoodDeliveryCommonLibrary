package com.fooddelivery.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AuctionTokenServiceTest {

    private AuctionTokenService auctionTokenService;
    private final String SECRET_KEY = "test-secret-key-for-auction-token-hmac-sha256";

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
    void testTamperedPrice() {
        UUID campaignId = UUID.randomUUID();
        UUID advertiserId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("1.50");
        UUID auctionId = UUID.randomUUID();
        
        String tokenStr = auctionTokenService.issue(campaignId, advertiserId, price, auctionId, Duration.ofMinutes(5));
        
        // Decode token, change price, and re-encode without updating signature
        String decoded = new String(Base64.getUrlDecoder().decode(tokenStr), StandardCharsets.UTF_8);
        String tamperedDecoded = decoded.replace("1.50", "0.01"); // try to underpay
        String tamperedTokenStr = Base64.getUrlEncoder().withoutPadding().encodeToString(tamperedDecoded.getBytes(StandardCharsets.UTF_8));
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            auctionTokenService.verify(tamperedTokenStr);
        });
        assertTrue(exception.getMessage().contains("Token verification failed"));
        assertTrue(exception.getCause().getMessage().contains("Invalid signature"));
    }

    @Test
    void testTamperedAdvertiser() {
        UUID campaignId = UUID.randomUUID();
        UUID advertiserId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("1.50");
        UUID auctionId = UUID.randomUUID();
        
        String tokenStr = auctionTokenService.issue(campaignId, advertiserId, price, auctionId, Duration.ofMinutes(5));
        
        String decoded = new String(Base64.getUrlDecoder().decode(tokenStr), StandardCharsets.UTF_8);
        String tamperedDecoded = decoded.replace(advertiserId.toString(), UUID.randomUUID().toString());
        String tamperedTokenStr = Base64.getUrlEncoder().withoutPadding().encodeToString(tamperedDecoded.getBytes(StandardCharsets.UTF_8));
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            auctionTokenService.verify(tamperedTokenStr);
        });
        assertTrue(exception.getCause().getMessage().contains("Invalid signature"));
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
        assertTrue(exception.getCause().getMessage().contains("Token expired"));
    }
}
