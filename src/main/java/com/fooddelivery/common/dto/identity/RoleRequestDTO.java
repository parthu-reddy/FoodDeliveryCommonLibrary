package com.fooddelivery.common.dto.identity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequestDTO {
    @NotBlank
    private String serviceName;
    @NotBlank
    private String roleName;
}
