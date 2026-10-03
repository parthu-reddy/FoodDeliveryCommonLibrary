package com.fooddelivery.common.client;

import com.fooddelivery.common.dto.organisation.MembershipDto;
import com.fooddelivery.common.enums.OrganisationPermission;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@FeignClient(name="identity-service", contextId = "organisation-service", fallbackFactory=OrganisationServiceClientFallback.class, configuration=com.fooddelivery.common.security.organisation.OrganisationFeignConfiguration.class)
public interface OrganisationServiceClient {
    @GetMapping("/api/v1/internal/organisations/{organisationId}/members/{userId}")
    MembershipDto getMembership(@PathVariable("organisationId") UUID org, @PathVariable("userId") UUID user);
    @GetMapping("/api/v1/internal/users/{userId}/organisations")
    List<MembershipDto> getUserOrganisations(@PathVariable("userId") UUID user);
    @GetMapping("/api/v1/internal/organisations/{organisationId}/members")
    List<UUID> getMembers(@PathVariable("organisationId") UUID org, @RequestParam("permission") OrganisationPermission permission);
}
