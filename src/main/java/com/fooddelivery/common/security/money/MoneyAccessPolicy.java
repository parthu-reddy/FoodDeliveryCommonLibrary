package com.fooddelivery.common.security.money;

import org.springframework.security.core.Authentication;
import java.util.UUID;

public interface MoneyAccessPolicy {
    boolean canAccessMoney(Authentication authentication, MoneyOwnerType ownerType, UUID ownerId);
}
