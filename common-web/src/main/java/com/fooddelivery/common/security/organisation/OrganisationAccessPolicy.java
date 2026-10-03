package com.fooddelivery.common.security.organisation;

import com.fooddelivery.common.enums.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import java.util.*;

public interface OrganisationAccessPolicy {
    boolean can(Authentication auth,UUID organisationId,OrganisationPermission permission);
    Optional<OrganisationRole> roleOf(Authentication auth,UUID organisationId);
    List<UUID> organisationsOf(Authentication auth,OrganisationPermission permission);
    default void require(Authentication auth,UUID organisationId,OrganisationPermission permission) {
        if (!can(auth,organisationId,permission)) { throw new AccessDeniedException("Organisation permission required"); }
    }
}
