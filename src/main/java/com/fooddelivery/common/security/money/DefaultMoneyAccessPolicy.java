package com.fooddelivery.common.security.money;

import com.fooddelivery.common.client.CampaignServiceClient;
import com.fooddelivery.common.client.RestaurantServiceClient;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class DefaultMoneyAccessPolicy implements MoneyAccessPolicy {

    private final RestaurantServiceClient restaurantServiceClient;
    private final CampaignServiceClient campaignServiceClient;

    /**
     * Caffeine cache keyed by "userId" → list of outlet IDs the user owns.
     * TTL 5 minutes so ownership changes propagate within a bounded window.
     */
    private final Cache<String, List<String>> outletOwnershipCache = Caffeine.newBuilder()
            .maximumSize(1_000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    /**
     * Caffeine cache keyed by "advertiserId" → owning userId.
     * TTL 5 minutes.
     */
    private final Cache<UUID, String> advertiserOwnerCache = Caffeine.newBuilder()
            .maximumSize(1_000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public DefaultMoneyAccessPolicy(RestaurantServiceClient restaurantServiceClient, CampaignServiceClient campaignServiceClient) {
        this.restaurantServiceClient = restaurantServiceClient;
        this.campaignServiceClient = campaignServiceClient;
    }

    @Override
    public boolean canAccessMoney(Authentication authentication, MoneyOwnerType ownerType, UUID ownerId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Admin and Service roles have full access
        boolean isPrivileged = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_SERVICE"));

        if (isPrivileged) {
            return true;
        }

        String userId = authentication.getName();

        switch (ownerType) {
            case CUSTOMER:
            case DRIVER:
                // For customer and driver, the ownerId is their userId
                return ownerId.toString().equals(userId);
            case RESTAURANT:
                // For restaurant, the ownerId is the outletId.
                // Check if the user owns this outlet via cached lookup.
                return isOutletOwner(ownerId, userId);
            case ADVERTISER:
                // For advertiser, the ownerId is the advertiserId.
                // Check if the user is the owner of this advertiser profile via cached lookup.
                return isAdvertiserOwner(ownerId, userId);
            default:
                return false;
        }
    }

    private boolean isOutletOwner(UUID outletId, String userId) {
        try {
            List<String> ownedOutlets = outletOwnershipCache.get(userId, key -> {
                List<String> outlets = restaurantServiceClient.getOwnerOutlets(key, "money-access-policy");
                return outlets != null ? outlets : List.of();
            });
            return ownedOutlets != null && ownedOutlets.contains(outletId.toString());
        } catch (Exception e) {
            // Fail closed: an unavailable dependency must not grant access.
            return false;
        }
    }

    private boolean isAdvertiserOwner(UUID advertiserId, String userId) {
        try {
            String ownerUserId = advertiserOwnerCache.get(advertiserId, key -> {
                var response = campaignServiceClient.getAdvertiserUserId(key);
                if (response != null && response.getBody() != null && response.getBody().get("userId") != null) {
                    return response.getBody().get("userId").toString();
                }
                return null;
            });
            return userId.equals(ownerUserId);
        } catch (Exception e) {
            // Fail closed: an unavailable dependency must not grant access.
            return false;
        }
    }
}
