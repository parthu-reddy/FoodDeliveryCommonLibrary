package com.fooddelivery.common.security.money;

import com.fooddelivery.common.client.CampaignServiceClient;
import com.fooddelivery.common.client.RestaurantServiceClient;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.fooddelivery.common.dto.restaurant.OutletOrganisationDto;
import com.fooddelivery.common.enums.OrganisationPermission;
import com.fooddelivery.common.security.organisation.OrganisationAccessPolicy;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component("moneyAccessPolicy")
public class DefaultMoneyAccessPolicy implements MoneyAccessPolicy {

    private final RestaurantServiceClient restaurantServiceClient;
    private final CampaignServiceClient campaignServiceClient;
    private final OrganisationAccessPolicy organisationAccessPolicy;

    /**
     * Cache immutable outlet routing only. Membership is rechecked through its bounded policy.
     */
    private final Cache<UUID, OutletOrganisationDto> outletOrganisationCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    /**
     * Caffeine cache keyed by "advertiserId" → owning userId.
     * TTL 5 minutes.
     */
    private final Cache<UUID, String> advertiserOwnerCache = Caffeine.newBuilder()
            .maximumSize(1_000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public DefaultMoneyAccessPolicy(RestaurantServiceClient restaurantServiceClient, CampaignServiceClient campaignServiceClient, OrganisationAccessPolicy organisationAccessPolicy) {
        this.restaurantServiceClient = restaurantServiceClient;
        this.campaignServiceClient = campaignServiceClient;
        this.organisationAccessPolicy = organisationAccessPolicy;
    }

    @Override
    public boolean canAccessMoney(Authentication authentication, MoneyOwnerType ownerType, UUID ownerId) {
        if (authentication == null || !authentication.isAuthenticated() || ownerType == null || ownerId == null) {
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
                return onOutlet(authentication, ownerId, OrganisationPermission.EARNINGS_VIEW);
            case ADVERTISER:
                // For advertiser, the ownerId is the advertiserId.
                // Check if the user is the owner of this advertiser profile via cached lookup.
                return isAdvertiserOwner(ownerId, userId);
            default:
                return false;
        }
    }

    @Override
    public boolean canManagePayouts(Authentication authentication, MoneyOwnerType ownerType, UUID ownerId) {
        if (authentication == null || !authentication.isAuthenticated() || ownerType == null || ownerId == null) { return false; }
        return ownerType == MoneyOwnerType.RESTAURANT
                ? onOutlet(authentication, ownerId, OrganisationPermission.PAYOUTS_MANAGE)
                : canAccessMoney(authentication, ownerType, ownerId);
    }

    private boolean onOutlet(Authentication authentication, UUID outletId, OrganisationPermission permission) {
        try {
            var outlet = outletOrganisationCache.get(outletId, key -> {
                var response = restaurantServiceClient.getOutletOrganisation(key);
                if (response == null || !key.equals(response.outletId()) || response.brandId() == null || response.organisationId() == null) {
                    throw new IllegalStateException("Invalid outlet organisation response");
                }
                return response;
            });
            return outlet != null && organisationAccessPolicy.can(authentication, outlet.organisationId(), permission);
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
