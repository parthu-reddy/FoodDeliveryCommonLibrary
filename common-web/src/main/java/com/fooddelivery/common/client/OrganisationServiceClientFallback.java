package com.fooddelivery.common.client;

import com.fooddelivery.common.dto.organisation.MembershipDto;
import com.fooddelivery.common.enums.OrganisationPermission;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/** Preserve a definitive 4xx: a circuit breaker must not turn revocation into an outage. */
@Component
public class OrganisationServiceClientFallback
        implements FallbackFactory<OrganisationServiceClient> {
    @Override
    public OrganisationServiceClient create(Throwable cause) {
        RuntimeException failure = new IllegalStateException("identity-service unavailable", cause);
        Throwable current = cause;
        while (current != null) {
            if (current instanceof feign.FeignException ex
                    && ex.status() >= 400
                    && ex.status() < 500) {
                failure = ex;
                break;
            }
            current = current.getCause();
        }
        final RuntimeException refused = failure;
        return new OrganisationServiceClient() {
            @Override
            public MembershipDto getMembership(UUID org, UUID user) {
                throw refused;
            }

            @Override
            public List<MembershipDto> getUserOrganisations(UUID user) {
                throw refused;
            }

            @Override
            public List<UUID> getMembers(UUID org, OrganisationPermission permission) {
                throw refused;
            }
        };
    }
}
