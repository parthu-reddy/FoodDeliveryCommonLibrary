package com.fooddelivery.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class AuctionTokenService {

    static final String DEV_SECRET_PREFIX = "dev-only-insecure-auction-secret";

    static final String DEV_SECRET = DEV_SECRET_PREFIX + "-override-in-production-12";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AuctionTokenService.class);

    private final byte[] secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuctionTokenService(@Value("${platform.auction.token-secret:}") String secretKeyStr,
                               org.springframework.core.env.Environment environment) {
        boolean production = java.util.Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (secretKeyStr == null || secretKeyStr.isBlank()) {
            if (production) {
                throw new IllegalStateException(
                        "platform.auction.token-secret is not set under the 'prod' profile. Auction prices "
                        + "would be signed with a publicly known development key. Set the "
                        + "AUCTION_TOKEN_SECRET environment variable to a 32-byte private value shared by "
                        + "BiddingEngine and UserTrackingService.");
            }
            LOG.warn("platform.auction.token-secret is not set; falling back to the development key. "
                    + "Set AUCTION_TOKEN_SECRET before any non-development use.");
            secretKeyStr = DEV_SECRET;
        } else if (secretKeyStr.startsWith(DEV_SECRET_PREFIX) && production) {
            throw new IllegalStateException(
                    "platform.auction.token-secret is the publicly known development key and the 'prod' "
                    + "profile is active. Set AUCTION_TOKEN_SECRET to a private value.");
        }
        
        byte[] keyBytes = secretKeyStr.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            this.secretKey = padded;
        } else if (keyBytes.length > 32) {
            byte[] truncated = new byte[32];
            System.arraycopy(keyBytes, 0, truncated, 0, 32);
            this.secretKey = truncated;
        } else {
            this.secretKey = keyBytes;
        }
    }

    public record AuctionToken(
            UUID campaignId,
            UUID advertiserId,
            String priceStr,
            UUID auctionId,
            long expiry
    ) {
        public BigDecimal getPriceAsBigDecimal() {
            return new BigDecimal(priceStr);
        }
    }

    public String issue(UUID campaignId, UUID advertiserId, String priceStr, UUID auctionId, Duration ttl) {
        long expiry = Instant.now().plus(ttl).toEpochMilli();
        String payload = campaignId + ":" + advertiserId + ":" + priceStr + ":" + auctionId + ":" + expiry;
        
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] iv = new byte[12];
            secureRandom.nextBytes(iv);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, parameterSpec);
            
            byte[] ciphertext = cipher.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            
            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
            
            return Base64.getUrlEncoder().withoutPadding().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt auction token", e);
        }
    }

    public String issue(UUID campaignId, UUID advertiserId, BigDecimal price, UUID auctionId, Duration ttl) {
        return issue(campaignId, advertiserId, price.toPlainString(), auctionId, ttl);
    }

    public AuctionToken verify(String tokenBase64) {
        try {
            byte[] combined = Base64.getUrlDecoder().decode(tokenBase64);
            if (combined.length < 12 + 16) {
                throw new IllegalArgumentException("Invalid token length");
            }
            
            byte[] iv = new byte[12];
            System.arraycopy(combined, 0, iv, 0, 12);
            byte[] ciphertext = new byte[combined.length - 12];
            System.arraycopy(combined, 12, ciphertext, 0, ciphertext.length);
            
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, parameterSpec);
            
            byte[] plaintext = cipher.doFinal(ciphertext);
            String payload = new String(plaintext, StandardCharsets.UTF_8);
            
            String[] parts = payload.split(":");
            if (parts.length != 5) {
                throw new IllegalArgumentException("Invalid payload structure");
            }

            UUID campaignId = UUID.fromString(parts[0]);
            UUID advertiserId = UUID.fromString(parts[1]);
            String priceStr = parts[2];
            UUID auctionId = UUID.fromString(parts[3]);
            long expiry = Long.parseLong(parts[4]);

            if (Instant.now().toEpochMilli() > expiry) {
                throw new IllegalArgumentException("Token expired");
            }

            return new AuctionToken(campaignId, advertiserId, priceStr, auctionId, expiry);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Token verification failed", e);
        }
    }
}
