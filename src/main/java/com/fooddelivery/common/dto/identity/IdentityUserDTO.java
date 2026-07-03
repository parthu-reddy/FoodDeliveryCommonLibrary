package com.fooddelivery.common.dto.identity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentityUserDTO {
    private UUID id;
    private String phoneNumber;
    private List<String> roles;
}
