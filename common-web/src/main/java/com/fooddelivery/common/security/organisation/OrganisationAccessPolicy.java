package com.fooddelivery.common.security.organisation;

import com.fooddelivery.common.enums.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import java.util.*;

public interface OrganisationAccessPolicy {
    boolean can(Authentication auth,UUID organisationId,OrganisationPermission permission);
    /** Check the named person's membership; never inherit the calling service's privileges. */
    boolean canUser(UUID userId,UUID organisationId,OrganisationPermission permission);
    /** Fresh lookup for prohibitions: an outage must never be interpreted as non-membership. */
    boolean canUserStrict(UUID userId,UUID organisationId,OrganisationPermission permission);
    Optional<OrganisationRole> roleOf(Authentication auth,UUID organisationId);
    List<UUID> organisationsOf(Authentication auth,OrganisationPermission permission);
    default void require(Authentication auth,UUID organisationId,OrganisationPermission permission) {
        if (!can(auth,organisationId,permission)) { throw new AccessDeniedException("Organisation permission required"); }
    }
}
