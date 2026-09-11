package com.fooddelivery.common.dto.identity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class IdentityUserDTO {
    private UUID id;
    private String phoneNumber;
    private List<String> roles;
}
