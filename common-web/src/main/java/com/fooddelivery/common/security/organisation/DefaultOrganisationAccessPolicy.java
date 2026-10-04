package com.fooddelivery.common.security.organisation;

import com.fooddelivery.common.client.OrganisationServiceClient;
import com.fooddelivery.common.dto.organisation.MembershipDto;
import com.fooddelivery.common.enums.*;
import com.github.benmanes.caffeine.cache.*;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.security.core.Authentication;
import java.time.Duration;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** A failed lookup never creates a membership. Stale entries retain the original fetch time. */
@lombok.extern.slf4j.Slf4j
public class DefaultOrganisationAccessPolicy implements OrganisationAccessPolicy {
    private static final Duration STALE_GRACE = Duration.ofSeconds(60);
    private static final Set<OrganisationPermission> OPERATIONAL=EnumSet.of(OrganisationPermission.ORG_VIEW,
        OrganisationPermission.STOCK_TOGGLE,OrganisationPermission.ORDERS_OPERATE,OrganisationPermission.EARNINGS_VIEW,
        OrganisationPermission.ADS_VIEW,OrganisationPermission.WALLET_VIEW);
    private final OrganisationServiceClient client;
    private final MeterRegistry metrics;
    private final Ticker ticker;
    private final Cache<Key,MembershipDto> fresh;
    private final Cache<Key,Fetched> stale;
    record Key(UUID organisationId,UUID userId) { }
    record Fetched(MembershipDto membership,long fetchedAt) { }

    public DefaultOrganisationAccessPolicy(OrganisationServiceClient client,MeterRegistry metrics) { this(client,metrics,Ticker.systemTicker()); }
    DefaultOrganisationAccessPolicy(OrganisationServiceClient client,MeterRegistry metrics,Ticker ticker) {
        this.client=client;this.metrics=metrics;this.ticker=ticker;
        fresh=Caffeine.newBuilder().maximumSize(10_000).expireAfterWrite(5,TimeUnit.SECONDS).ticker(ticker).build();
        stale=Caffeine.newBuilder().maximumSize(10_000).expireAfterWrite(STALE_GRACE).ticker(ticker).build();
    }
    @Override public boolean can(Authentication auth,UUID org,OrganisationPermission permission) {
        if (auth==null || !auth.isAuthenticated() || org==null || permission==null) { return decision(permission,false); }
        if (has(auth,"ROLE_SERVICE")) { return decision(permission,true); }
        // Platform admins never acquire organisation write powers via a coincidental membership.
        if (has(auth,"ROLE_ADMIN")) { return decision(permission,permission.readOnly()); }
        UUID user=person(auth); if (user==null) { return decision(permission,false); }
        return decision(permission,eligible(lookup(new Key(org,user),OPERATIONAL.contains(permission)),permission));
    }
    @Override public Optional<OrganisationRole> roleOf(Authentication auth,UUID org) {
        UUID user=person(auth); if (user==null || org==null) { return Optional.empty(); }
        var membership=lookup(new Key(org,user),false);
        return eligible(membership,OrganisationPermission.ORG_VIEW)?Optional.of(membership.role()):Optional.empty();
    }
    @Override public boolean canUser(UUID user,UUID org,OrganisationPermission permission) {
        if(user==null || org==null || permission==null) { return decision(permission,false); }
        return decision(permission,eligible(lookup(new Key(org,user),OPERATIONAL.contains(permission)),permission));
    }
    @Override public boolean canUserStrict(UUID user,UUID org,OrganisationPermission permission) {
        if (user==null || org==null || permission==null) { return decision(permission,false); }
        try {
            var membership=client.getMembership(org,user);
            if (!valid(membership,org,user)) { throw new IllegalStateException("Invalid membership response"); }
            return decision(permission,eligible(membership,permission));
        } catch (feign.FeignException.NotFound absent) {
            return decision(permission,false);
        } catch (RuntimeException unavailable) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Membership verification unavailable", unavailable);
        }
    }
    @Override public List<UUID> organisationsOf(Authentication auth,OrganisationPermission permission) {
        UUID user=person(auth);if (user==null || permission==null) { return List.of(); }
        try {
            var memberships=client.getUserOrganisations(user);
            if (memberships==null) { throw new IllegalStateException("Missing memberships response"); }
            return memberships.stream().filter(m -> valid(m,m.organisationId(),user) && eligible(m,permission))
                .filter(m -> !has(auth,"ROLE_ADMIN") || permission.readOnly()).map(MembershipDto::organisationId).distinct().toList();
        } catch (Exception ex) {
            metrics.counter("organisation.access.decisions","permission",permission.name(),"result","error").increment();
            log.warn("Organisation list lookup failed userId={}",user); return List.of();
        }
    }
    private MembershipDto lookup(Key key,boolean allowStale) {
        var hit=fresh.getIfPresent(key);
        if (hit!=null) { cacheMetric("hit");return hit; }
        cacheMetric("miss");
        try {
            // Caffeine coalesces simultaneous requests for the same member.
            return fresh.get(key,k -> {
                var membership=client.getMembership(k.organisationId(),k.userId());
                if (!valid(membership,k.organisationId(),k.userId())) { stale.invalidate(k);throw new IllegalStateException("Invalid membership response"); }
                stale.put(k,new Fetched(membership,ticker.read())); return membership;
            });
        } catch (Exception ex) {
            // A confirmed 404 revokes the old membership; only an outage permits stale access.
            if (ex instanceof feign.FeignException failure && failure.status()>=400 && failure.status()<500) { stale.invalidate(key);return null; }
            metrics.counter("organisation.access.decisions","permission","LOOKUP","result","error").increment();
            log.warn("Organisation membership lookup failed organisationId={} userId={}",key.organisationId(),key.userId());
            var previous=stale.getIfPresent(key);
            if (allowStale && previous!=null && ticker.read()-previous.fetchedAt() < STALE_GRACE.toNanos()) {
                cacheMetric("stale");return previous.membership();
            }
            return null;
        }
    }
    private static boolean valid(MembershipDto m,UUID org,UUID user) {
        return m!=null && org.equals(m.organisationId()) && user.equals(m.userId()) && m.role()!=null && m.status()!=null && m.organisationStatus()!=null;
    }
    static boolean eligible(MembershipDto m,OrganisationPermission p) {
        return m!=null && m.status()==MembershipStatus.ACTIVE && m.organisationStatus()!=OrganisationStatus.CLOSED
            && (m.organisationStatus()!=OrganisationStatus.SUSPENDED || p==OrganisationPermission.ORG_VIEW) && m.role().grants(p);
    }
    private boolean decision(OrganisationPermission p,boolean allowed) {
        metrics.counter("organisation.access.decisions","permission",p==null?"INVALID":p.name(),"result",allowed?"allow":"deny").increment(); return allowed;
    }
    private void cacheMetric(String result) { metrics.counter("organisation.access.cache","result",result).increment(); }
    static boolean has(Authentication auth,String role) { return auth!=null && auth.getAuthorities().stream().anyMatch(a -> role.equals(a.getAuthority())); }
    static UUID person(Authentication auth) {
        if (auth==null || !auth.isAuthenticated()) { return null; }
        try { return UUID.fromString(auth.getName()); } catch (IllegalArgumentException ex) { return null; }
    }
}
