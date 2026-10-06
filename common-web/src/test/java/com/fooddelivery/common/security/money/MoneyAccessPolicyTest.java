package com.fooddelivery.common.security.money;

import com.fooddelivery.common.client.*;
import com.fooddelivery.common.dto.restaurant.OutletOrganisationDto;
import com.fooddelivery.common.enums.*;
import com.fooddelivery.common.security.organisation.OrganisationAccessPolicy;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MoneyAccessPolicyTest {
    RestaurantServiceClient restaurants = mock(RestaurantServiceClient.class);
    OrganisationAccessPolicy organisations = mock(OrganisationAccessPolicy.class);
    UUID outlet = UUID.randomUUID(), brand = UUID.randomUUID(), organisation = UUID.randomUUID(), user = UUID.randomUUID();
    Authentication auth = auth("RESTAURANT");
    OrganisationRole membershipRole = OrganisationRole.OWNER;
    DefaultMoneyAccessPolicy policy;
    Authentication auth(String role) {
        return new UsernamePasswordAuthenticationToken(user.toString(), null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }
    @BeforeEach void setup() {
        policy = new DefaultMoneyAccessPolicy(restaurants, organisations);
        when(restaurants.getOutletOrganisation(outlet)).thenReturn(new OutletOrganisationDto(outlet, brand, organisation));
        when(organisations.can(eq(auth), eq(organisation), any())).thenAnswer(call ->
                membershipRole != null && membershipRole.grants(call.getArgument(2, OrganisationPermission.class)));
    }
    @Test void customerAndDriverCanReadTheirOwnMoneyOnly() {
        for (var type : List.of(MoneyOwnerType.CUSTOMER, MoneyOwnerType.DRIVER)) {
            assertTrue(policy.canAccessMoney(auth, type, user));
            assertFalse(policy.canAccessMoney(auth, type, UUID.randomUUID()));
        }
    }
    @Test void platformAdminReadsDoNotGrantOrganisationPayoutWrites() {
        var admin = auth("ADMIN");
        assertTrue(policy.canAccessMoney(admin, MoneyOwnerType.RESTAURANT, outlet));
        assertFalse(policy.canManagePayouts(admin, MoneyOwnerType.RESTAURANT, outlet));
    }
    @ParameterizedTest @EnumSource(OrganisationRole.class)
    void restaurantMoneyUsesExactEarningsAndPayoutPermissions(OrganisationRole role) {
        membershipRole = role;
        assertEquals(role != OrganisationRole.STAFF, policy.canAccessMoney(auth, MoneyOwnerType.RESTAURANT, outlet));
        assertEquals(role == OrganisationRole.OWNER || role == OrganisationRole.ADMIN,
                policy.canManagePayouts(auth, MoneyOwnerType.RESTAURANT, outlet));
        verify(organisations).can(auth, organisation, OrganisationPermission.EARNINGS_VIEW);
        verify(organisations).can(auth, organisation, OrganisationPermission.PAYOUTS_MANAGE);
    }
    @Test void nonmemberAndUnrelatedOutletAreDenied() {
        membershipRole = null;
        assertFalse(policy.canAccessMoney(auth, MoneyOwnerType.RESTAURANT, outlet));
        assertFalse(policy.canAccessMoney(auth, MoneyOwnerType.RESTAURANT, UUID.randomUUID()));
    }
    @Test void outletCacheDoesNotCachePermissionOrMembership() {
        assertTrue(policy.canAccessMoney(auth, MoneyOwnerType.RESTAURANT, outlet));
        membershipRole = OrganisationRole.STAFF;
        assertFalse(policy.canAccessMoney(auth, MoneyOwnerType.RESTAURANT, outlet));
        verify(restaurants, times(1)).getOutletOrganisation(outlet);
        verify(organisations, times(2)).can(auth, organisation, OrganisationPermission.EARNINGS_VIEW);
    }
    @Test void unavailableIdentityOrRestaurantFailsClosed() {
        when(organisations.can(eq(auth), eq(organisation), any())).thenThrow(new IllegalStateException("Identity unavailable without cached membership"));
        assertFalse(policy.canAccessMoney(auth, MoneyOwnerType.RESTAURANT, outlet));
        assertFalse(policy.canManagePayouts(auth, MoneyOwnerType.RESTAURANT, outlet));
        policy = new DefaultMoneyAccessPolicy(restaurants, organisations);
        when(restaurants.getOutletOrganisation(outlet)).thenThrow(new IllegalStateException("Restaurant unavailable"));
        assertFalse(policy.canAccessMoney(auth, MoneyOwnerType.RESTAURANT, outlet));
    }
    @Test void malformedOutletResponseCannotAuthorizeAnotherOrganisation() {
        when(restaurants.getOutletOrganisation(outlet)).thenReturn(new OutletOrganisationDto(UUID.randomUUID(), brand, organisation));
        assertFalse(policy.canAccessMoney(auth, MoneyOwnerType.RESTAURANT, outlet));
        verifyNoInteractions(organisations);
    }
    @Test void missingInputsAndUnauthenticatedCallersDeny() {
        assertFalse(policy.canAccessMoney(null, MoneyOwnerType.RESTAURANT, outlet));
        assertFalse(policy.canAccessMoney(auth, null, outlet));
        assertFalse(policy.canManagePayouts(auth, MoneyOwnerType.RESTAURANT, null));
        assertFalse(policy.canAccessMoney(new UsernamePasswordAuthenticationToken(user.toString(), null), MoneyOwnerType.RESTAURANT, outlet));
    }
    @ParameterizedTest @EnumSource(OrganisationRole.class)
    void businessWalletNeedsWalletViewInThatOrganisation(OrganisationRole role) {
        membershipRole = role;
        assertEquals(role != OrganisationRole.STAFF, policy.canAccessMoney(auth, MoneyOwnerType.BUSINESS, organisation));
        verify(organisations).can(auth, organisation, OrganisationPermission.WALLET_VIEW);
        verifyNoInteractions(restaurants);
    }
    @Test void businessWalletDeniesNonmembersAndOtherOrganisations() {
        membershipRole = null;
        assertFalse(policy.canAccessMoney(auth, MoneyOwnerType.BUSINESS, organisation));
        membershipRole = OrganisationRole.OWNER;
        assertFalse(policy.canAccessMoney(auth, MoneyOwnerType.BUSINESS, UUID.randomUUID()));
    }
    @Test void businessWalletFailsClosedWhenIdentityIsUnavailable() {
        when(organisations.can(eq(auth), eq(organisation), any())).thenThrow(new IllegalStateException("Identity unavailable without cached membership"));
        assertFalse(policy.canAccessMoney(auth, MoneyOwnerType.BUSINESS, organisation));
    }
}
