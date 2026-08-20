package com.fooddelivery.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class AuctionTokenService {

    /** Development fallback. Deployment/application.yml ships the same value for compose runs. */
    static final String DEV_SECRET = "dev-only-insecure-auction-secret-override-in-production";

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AuctionTokenService.class);

    private final String secretKey;

    /**
     * BiddingEngine signs auction prices with this key and UserTrackingService verifies them, so the
     * two must share the same value. Outside production an unset key falls back to a well-known
     * development secret so local runs and tests work without extra setup; under the {@code prod}
     * profile an unset key is fatal, because signing with a publicly known key is the same as not
     * signing at all.
     */
    public AuctionTokenService(@Value("${platform.auction.token-secret:}") String secretKey,
                               org.springframework.core.env.Environment environment) {
        boolean production = java.util.Arrays.asList(environment.getActiveProfiles()).contains("prod");
        if (secretKey == null || secretKey.isBlank()) {
            if (production) {
                throw new IllegalStateException(
                        "platform.auction.token-secret is not set under the 'prod' profile. Auction prices "
                        + "would be signed with a publicly known development key. Set the "
                        + "AUCTION_TOKEN_SECRET environment variable to a private value shared by "
                        + "BiddingEngine and UserTrackingService.");
            }
            LOG.warn("platform.auction.token-secret is not set; falling back to the development key. "
                    + "Set AUCTION_TOKEN_SECRET before any non-development use.");
            secretKey = DEV_SECRET;
        } else if (DEV_SECRET.equals(secretKey) && production) {
            throw new IllegalStateException(
                    "platform.auction.token-secret is the publicly known development key and the 'prod' "
                    + "profile is active. Set AUCTION_TOKEN_SECRET to a private value.");
        }
        this.secretKey = secretKey;
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
        String signature = sign(payload);
        String token = payload + ":" + signature;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    public String issue(UUID campaignId, UUID advertiserId, BigDecimal price, UUID auctionId, Duration ttl) {
        return issue(campaignId, advertiserId, price.toPlainString(), auctionId, ttl);
    }

    public AuctionToken verify(String tokenBase64) {
        try {
            String token = new String(Base64.getUrlDecoder().decode(tokenBase64), StandardCharsets.UTF_8);
            int lastColon = token.lastIndexOf(':');
            if (lastColon == -1) {
                throw new IllegalArgumentException("Invalid token format");
            }
            String payload = token.substring(0, lastColon);
            String providedSignature = token.substring(lastColon + 1);

            String expectedSignature = sign(payload);
            if (!MessageDigest.isEqual(providedSignature.getBytes(StandardCharsets.UTF_8), expectedSignature.getBytes(StandardCharsets.UTF_8))) {
                throw new IllegalArgumentException("Invalid signature");
            }

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
        } catch (Exception e) {
            throw new IllegalArgumentException("Token verification failed", e);
        }
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to calculate HMAC", e);
        }
    }
}
