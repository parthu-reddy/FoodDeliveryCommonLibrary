package com.fooddelivery.common.security.money;

import com.fooddelivery.common.client.RestaurantServiceClient;
import com.fooddelivery.common.dto.restaurant.OutletOrganisationDto;
import com.fooddelivery.common.enums.OrganisationPermission;
import com.fooddelivery.common.security.organisation.OrganisationAccessPolicy;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component("moneyAccessPolicy")
public class DefaultMoneyAccessPolicy implements MoneyAccessPolicy {

    private final RestaurantServiceClient restaurantServiceClient;
    private final OrganisationAccessPolicy organisationAccessPolicy;

    /** Cache immutable outlet routing only. Membership is rechecked through its bounded policy. */
    private final Cache<UUID, OutletOrganisationDto> outletOrganisationCache =
            Caffeine.newBuilder()
                    .maximumSize(10_000)
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .build();

    /**
     * {@code restaurantServiceClient} is null in a service that does not register it as a Feign client: such a
     * service can never authorise restaurant (outlet) money and says so (deny + error log). Before, a component
     * fallback that implemented the client stood in for it and denied silently — the pattern that hid a missing
     * client registration in WalletService (BusinessPlatform W2/A2).
     */
    public DefaultMoneyAccessPolicy(
            @org.springframework.lang.Nullable RestaurantServiceClient restaurantServiceClient,
            OrganisationAccessPolicy organisationAccessPolicy) {
        this.restaurantServiceClient = restaurantServiceClient;
        this.organisationAccessPolicy = organisationAccessPolicy;
    }

    @Override
    public boolean canAccessMoney(
            Authentication authentication, MoneyOwnerType ownerType, UUID ownerId) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || ownerType == null
                || ownerId == null) {
            return false;
        }

        // Admin and Service roles have full access
        boolean isPrivileged =
                authentication.getAuthorities().stream()
                        .anyMatch(
                                a ->
                                        a.getAuthority().equals("ROLE_ADMIN")
                                                || a.getAuthority().equals("ROLE_SERVICE"));

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
            case BUSINESS:
                // A business wallet belongs to an organisation; ownerId is the organisation id.
                return onOrganisation(authentication, ownerId, OrganisationPermission.WALLET_VIEW);
            default:
                return false;
        }
    }

    @Override
    public boolean canManagePayouts(
            Authentication authentication, MoneyOwnerType ownerType, UUID ownerId) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || ownerType == null
                || ownerId == null) {
            return false;
        }
        return ownerType == MoneyOwnerType.RESTAURANT
                ? onOutlet(authentication, ownerId, OrganisationPermission.PAYOUTS_MANAGE)
                : canAccessMoney(authentication, ownerType, ownerId);
    }

    private boolean onOutlet(
            Authentication authentication, UUID outletId, OrganisationPermission permission) {
        if (restaurantServiceClient == null) {
            org.slf4j.LoggerFactory.getLogger(DefaultMoneyAccessPolicy.class).error(
                    "RESTAURANT_MONEY_UNAVAILABLE outletId={}: this service does not register RestaurantServiceClient; access denied", outletId);
            return false;
        }
        try {
            var outlet =
                    outletOrganisationCache.get(
                            outletId,
                            key -> {
                                var response = restaurantServiceClient.getOutletOrganisation(key);
                                if (response == null
                                        || !key.equals(response.outletId())
                                        || response.brandId() == null
                                        || response.organisationId() == null) {
                                    throw new IllegalStateException(
                                            "Invalid outlet organisation response");
                                }
                                return response;
                            });
            return outlet != null
                    && organisationAccessPolicy.can(
                            authentication, outlet.organisationId(), permission);
        } catch (Exception e) {
            // Fail closed: an unavailable dependency must not grant access.
            return false;
        }
    }

    private boolean onOrganisation(
            Authentication authentication, UUID organisationId, OrganisationPermission permission) {
        try {
            return organisationAccessPolicy.can(authentication, organisationId, permission);
        } catch (Exception e) {
            // Fail closed: an unavailable dependency must not grant access.
            return false;
        }
    }
}
