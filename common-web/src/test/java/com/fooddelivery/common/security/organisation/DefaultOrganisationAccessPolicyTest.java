package com.fooddelivery.common.security.organisation;

import com.fooddelivery.common.client.*;
import com.fooddelivery.common.dto.organisation.MembershipDto;
import com.fooddelivery.common.enums.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultOrganisationAccessPolicyTest {
    UUID org=UUID.randomUUID(),user=UUID.randomUUID();
    OrganisationServiceClient client;
    AtomicLong nanos;
    DefaultOrganisationAccessPolicy policy;
    Authentication auth(String... roles) {return new UsernamePasswordAuthenticationToken(user.toString(),null,Arrays.stream(roles).map(SimpleGrantedAuthority::new).toList());}
    MembershipDto member(OrganisationRole role,OrganisationStatus status,MembershipStatus membership) {return new MembershipDto(org,status,user,role,membership);}
    @BeforeEach void setup(){client=mock(OrganisationServiceClient.class);nanos=new AtomicLong();policy=new DefaultOrganisationAccessPolicy(client,new SimpleMeterRegistry(),nanos::get);}
    @Test void prohibitionLookupNeverUsesAStaleDenialOrTreatsAnOutageAsAbsence() {
        when(client.getMembership(org,user)).thenReturn(member(OrganisationRole.STAFF,OrganisationStatus.ACTIVE,MembershipStatus.ACTIVE));
        assertTrue(policy.canUserStrict(user,org,OrganisationPermission.ORG_VIEW));
        when(client.getMembership(org,user)).thenThrow(new IllegalStateException("unavailable"));
        assertThrows(ResponseStatusException.class,
                () -> policy.canUserStrict(user,org,OrganisationPermission.ORG_VIEW));
        verify(client,times(2)).getMembership(org,user);
    }
    @Test void prohibitionLookupDistinguishesConfirmedAbsenceFromMalformedMembership() {
        var request=feign.Request.create(feign.Request.HttpMethod.GET,"http://identity/membership",Map.of(),null,java.nio.charset.StandardCharsets.UTF_8,null);
        when(client.getMembership(org,user)).thenThrow(new feign.FeignException.NotFound("absent",request,null,Map.of()));
        assertFalse(policy.canUserStrict(user,org,OrganisationPermission.ORG_VIEW));
        doReturn(null).when(client).getMembership(org,user);
        assertThrows(ResponseStatusException.class,
                () -> policy.canUserStrict(user,org,OrganisationPermission.ORG_VIEW));
    }
    @Test void serviceBypassesAndPlatformAdminOnlyReads(){
        for(var p:OrganisationPermission.values()){
            assertTrue(policy.can(auth("ROLE_SERVICE"),org,p));
            assertEquals(p.readOnly(),policy.can(auth("ROLE_ADMIN"),org,p));
        }
        verifyNoInteractions(client);
    }
    @Test void anInternalCallerMustCheckTheNamedPersonsMembershipAndPermission(){
        when(client.getMembership(org,user)).thenReturn(member(OrganisationRole.STAFF,OrganisationStatus.ACTIVE,MembershipStatus.ACTIVE));
        SecurityContextHolder.getContext().setAuthentication(auth("ROLE_SERVICE"));
        try {
            assertTrue(policy.canUser(user,org,OrganisationPermission.ORDERS_OPERATE));
            assertFalse(policy.canUser(user,org,OrganisationPermission.EARNINGS_VIEW));
            assertFalse(policy.canUser(UUID.randomUUID(),org,OrganisationPermission.ORDERS_OPERATE));
            assertFalse(policy.canUser(null,org,OrganisationPermission.ORG_VIEW));
            nanos.set(TimeUnit.SECONDS.toNanos(6));
            when(client.getMembership(org,user)).thenReturn(member(OrganisationRole.STAFF,OrganisationStatus.ACTIVE,MembershipStatus.REMOVED));
            assertFalse(policy.canUser(user,org,OrganisationPermission.ORDERS_OPERATE));
        } finally { SecurityContextHolder.clearContext(); }
    }
    @Test void staffMayOperateButCannotEditMenu(){
        when(client.getMembership(org,user)).thenReturn(member(OrganisationRole.STAFF,OrganisationStatus.ACTIVE,MembershipStatus.ACTIVE));
        assertTrue(policy.can(auth("ROLE_RESTAURANT"),org,OrganisationPermission.STOCK_TOGGLE));
        assertFalse(policy.can(auth("ROLE_RESTAURANT"),org,OrganisationPermission.MENU_MANAGE));
        assertFalse(policy.can(auth("ROLE_RESTAURANT"),org,OrganisationPermission.EARNINGS_VIEW));
    }
    @Test void suspendedOnlyAllowsOrgView(){
        when(client.getMembership(org,user)).thenReturn(member(OrganisationRole.OWNER,OrganisationStatus.SUSPENDED,MembershipStatus.ACTIVE));
        for(var p:OrganisationPermission.values()){assertEquals(p==OrganisationPermission.ORG_VIEW,policy.can(auth("ROLE_RESTAURANT"),org,p),p.name());}
    }
    @Test void removedClosedAndUnknownMembershipsDeny(){
        when(client.getMembership(org,user)).thenReturn(member(OrganisationRole.OWNER,OrganisationStatus.ACTIVE,MembershipStatus.REMOVED));
        assertFalse(policy.can(auth("ROLE_CUSTOMER"),org,OrganisationPermission.ORG_VIEW));
        nanos.addAndGet(TimeUnit.SECONDS.toNanos(6));
        when(client.getMembership(org,user)).thenReturn(member(OrganisationRole.OWNER,OrganisationStatus.CLOSED,MembershipStatus.ACTIVE));
        assertFalse(policy.can(auth("ROLE_CUSTOMER"),org,OrganisationPermission.ORG_VIEW));
        assertFalse(policy.can(null,org,OrganisationPermission.ORG_VIEW));
    }
    @Test void cacheExpiresAtFiveSeconds(){
        when(client.getMembership(org,user)).thenReturn(member(OrganisationRole.OWNER,OrganisationStatus.ACTIVE,MembershipStatus.ACTIVE));
        assertTrue(policy.can(auth("ROLE_CUSTOMER"),org,OrganisationPermission.ORG_VIEW));
        nanos.set(TimeUnit.SECONDS.toNanos(4));assertTrue(policy.can(auth("ROLE_CUSTOMER"),org,OrganisationPermission.ORG_VIEW));
        verify(client,times(1)).getMembership(org,user);
        nanos.set(TimeUnit.SECONDS.toNanos(5));assertTrue(policy.can(auth("ROLE_CUSTOMER"),org,OrganisationPermission.ORG_VIEW));
        verify(client,times(2)).getMembership(org,user);
    }
    @Test void outageAllowsOnlyBoundedOperationalPermissions(){
        when(client.getMembership(org,user)).thenReturn(member(OrganisationRole.OWNER,OrganisationStatus.ACTIVE,MembershipStatus.ACTIVE));
        assertTrue(policy.can(auth("ROLE_CUSTOMER"),org,OrganisationPermission.ORG_VIEW));
        nanos.set(TimeUnit.SECONDS.toNanos(6));when(client.getMembership(org,user)).thenThrow(new IllegalStateException("identity-service unavailable"));
        Set<OrganisationPermission> operational=EnumSet.of(OrganisationPermission.ORG_VIEW,OrganisationPermission.STOCK_TOGGLE,
            OrganisationPermission.ORDERS_OPERATE,OrganisationPermission.EARNINGS_VIEW,OrganisationPermission.ADS_VIEW,OrganisationPermission.WALLET_VIEW);
        for(var p:OrganisationPermission.values()){assertEquals(operational.contains(p),policy.can(auth("ROLE_CUSTOMER"),org,p),p.name());}
        assertTrue(policy.roleOf(auth("ROLE_CUSTOMER"),org).isEmpty());
        nanos.set(TimeUnit.SECONDS.toNanos(60));assertFalse(policy.can(auth("ROLE_CUSTOMER"),org,OrganisationPermission.ORDERS_OPERATE));
        nanos.set(TimeUnit.SECONDS.toNanos(61));assertFalse(policy.can(auth("ROLE_CUSTOMER"),org,OrganisationPermission.ORG_VIEW));
    }
    @Test void outageWithoutPreviousMembershipAndMalformedResponseDeny(){
        doThrow(new IllegalStateException("offline")).when(client).getMembership(org,user);
        assertFalse(policy.can(auth("ROLE_CUSTOMER"),org,OrganisationPermission.ORG_VIEW));
        doReturn(new MembershipDto(UUID.randomUUID(),OrganisationStatus.ACTIVE,user,OrganisationRole.OWNER,MembershipStatus.ACTIVE)).when(client).getMembership(org,user);
        assertFalse(policy.can(auth("ROLE_CUSTOMER"),org,OrganisationPermission.ORG_VIEW));
    }
    @Test void confirmedRevocationClearsStaleEvenThroughFallback(){
        when(client.getMembership(org,user)).thenReturn(member(OrganisationRole.OWNER,OrganisationStatus.ACTIVE,MembershipStatus.ACTIVE));
        assertTrue(policy.can(auth("ROLE_CUSTOMER"),org,OrganisationPermission.ORG_VIEW));
        nanos.set(TimeUnit.SECONDS.toNanos(6));
        var notFound=feign.FeignException.errorStatus("membership",feign.Response.builder().status(404).reason("absent")
            .request(feign.Request.create(feign.Request.HttpMethod.GET,"http://identity/membership",Map.of(),null,java.nio.charset.StandardCharsets.UTF_8,null)).headers(Map.of()).build());
        var fallback=new OrganisationServiceClientFallback().create(notFound);
        assertThrows(feign.FeignException.class,()->fallback.getMembership(org,user));
        when(client.getMembership(org,user)).thenThrow(notFound);
        assertFalse(policy.can(auth("ROLE_CUSTOMER"),org,OrganisationPermission.ORDERS_OPERATE));
        doThrow(new IllegalStateException("offline")).when(client).getMembership(org,user);
        assertFalse(policy.can(auth("ROLE_CUSTOMER"),org,OrganisationPermission.ORDERS_OPERATE));
    }
    @Test void listChecksUserRoleAndOrganisationStatus(){
        when(client.getUserOrganisations(user)).thenReturn(List.of(member(OrganisationRole.MANAGER,OrganisationStatus.ACTIVE,MembershipStatus.ACTIVE)));
        assertEquals(List.of(org),policy.organisationsOf(auth("ROLE_RESTAURANT"),OrganisationPermission.MENU_MANAGE));
        assertTrue(policy.organisationsOf(auth("ROLE_RESTAURANT"),OrganisationPermission.WALLET_TOPUP).isEmpty());
        assertTrue(policy.organisationsOf(auth("ROLE_ADMIN"),OrganisationPermission.MENU_MANAGE).isEmpty());
    }
    @Test void fallbackCannotTurnAnOutageIntoAnEmptySuccess(){
        var fallback=new OrganisationServiceClientFallback().create(new IllegalStateException("offline"));
        assertThrows(IllegalStateException.class,()->fallback.getMembership(org,user));
        assertThrows(IllegalStateException.class,()->fallback.getUserOrganisations(user));
        assertThrows(IllegalStateException.class,()->fallback.getMembers(org,OrganisationPermission.ORG_VIEW));
    }
}
