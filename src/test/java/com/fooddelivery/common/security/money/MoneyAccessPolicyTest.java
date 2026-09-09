package com.fooddelivery.common.security.money;

import com.fooddelivery.common.client.CampaignServiceClient;
import com.fooddelivery.common.client.RestaurantServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class MoneyAccessPolicyTest {

    @Mock
    private RestaurantServiceClient restaurantServiceClient;

    @Mock
    private CampaignServiceClient campaignServiceClient;

    @Mock
    private Authentication authentication;

    private DefaultMoneyAccessPolicy moneyAccessPolicy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        moneyAccessPolicy = new DefaultMoneyAccessPolicy(restaurantServiceClient, campaignServiceClient);
    }

    @Test
    void testCustomerAccessOwnMoney() {
        UUID ownerId = UUID.randomUUID();
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(ownerId.toString());
        org.mockito.Mockito.<java.util.Collection<? extends org.springframework.security.core.GrantedAuthority>>doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))).when(authentication).getAuthorities();

        assertTrue(moneyAccessPolicy.canAccessMoney(authentication, MoneyOwnerType.CUSTOMER, ownerId));
    }

    @Test
    void testCustomerCannotAccessOtherMoney() {
        UUID ownerId = UUID.randomUUID();
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(UUID.randomUUID().toString());
        org.mockito.Mockito.<java.util.Collection<? extends org.springframework.security.core.GrantedAuthority>>doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_CUSTOMER"))).when(authentication).getAuthorities();

        assertFalse(moneyAccessPolicy.canAccessMoney(authentication, MoneyOwnerType.CUSTOMER, ownerId));
    }

    @Test
    void testAdminCanAccessAnyMoney() {
        UUID ownerId = UUID.randomUUID();
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(UUID.randomUUID().toString());
        org.mockito.Mockito.<java.util.Collection<? extends org.springframework.security.core.GrantedAuthority>>doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(authentication).getAuthorities();

        assertTrue(moneyAccessPolicy.canAccessMoney(authentication, MoneyOwnerType.CUSTOMER, ownerId));
    }

    @Test
    void testRestaurantOwnerCanAccessRestaurantMoney() {
        UUID outletId = UUID.randomUUID();
        String userId = UUID.randomUUID().toString();
        
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userId);
        org.mockito.Mockito.<java.util.Collection<? extends org.springframework.security.core.GrantedAuthority>>doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_RESTAURANT"))).when(authentication).getAuthorities();
        
        when(restaurantServiceClient.getOwnerOutlets(anyString(), anyString())).thenReturn(java.util.List.of(outletId.toString()));

        assertTrue(moneyAccessPolicy.canAccessMoney(authentication, MoneyOwnerType.RESTAURANT, outletId));
    }

    /**
     * The case the whole outlet-ownership lookup exists for, and the one this suite did not have.
     *
     * <p>Found on 2026-09-09 by performing the break-test Phase 1's validation.md specified: making
     * the RESTAURANT branch return true without consulting the client left all four tests green.
     * A restaurant user could read any outlet's earnings and nothing would have caught it.
     */
    @Test
    void testRestaurantOwnerCannotAccessAnotherOutletsMoney() {
        UUID theirOutlet = UUID.randomUUID();
        UUID someoneElsesOutlet = UUID.randomUUID();
        String userId = UUID.randomUUID().toString();

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userId);
        org.mockito.Mockito.<java.util.Collection<? extends org.springframework.security.core.GrantedAuthority>>doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_RESTAURANT"))).when(authentication).getAuthorities();

        when(restaurantServiceClient.getOwnerOutlets(anyString(), anyString()))
                .thenReturn(java.util.List.of(theirOutlet.toString()));

        assertFalse(moneyAccessPolicy.canAccessMoney(authentication, MoneyOwnerType.RESTAURANT, someoneElsesOutlet),
                "a restaurant user must not read another outlet's money");
    }

    /** An owner of no outlets owns no outlet's money. */
    @Test
    void testRestaurantUserWithNoOutletsIsRefused() {
        UUID outletId = UUID.randomUUID();

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(UUID.randomUUID().toString());
        org.mockito.Mockito.<java.util.Collection<? extends org.springframework.security.core.GrantedAuthority>>doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_RESTAURANT"))).when(authentication).getAuthorities();

        when(restaurantServiceClient.getOwnerOutlets(anyString(), anyString())).thenReturn(java.util.List.of());

        assertFalse(moneyAccessPolicy.canAccessMoney(authentication, MoneyOwnerType.RESTAURANT, outletId));
    }

    /** The policy fails closed: an unreachable restaurant service is not an authorisation. */
    @Test
    void testAnUnreachableRestaurantServiceDeniesAccess() {
        UUID outletId = UUID.randomUUID();

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(UUID.randomUUID().toString());
        org.mockito.Mockito.<java.util.Collection<? extends org.springframework.security.core.GrantedAuthority>>doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_RESTAURANT"))).when(authentication).getAuthorities();

        when(restaurantServiceClient.getOwnerOutlets(anyString(), anyString()))
                .thenThrow(new RuntimeException("restaurant service unavailable"));

        assertFalse(moneyAccessPolicy.canAccessMoney(authentication, MoneyOwnerType.RESTAURANT, outletId),
                "an unavailable ownership lookup must deny, not allow");
    }
}
