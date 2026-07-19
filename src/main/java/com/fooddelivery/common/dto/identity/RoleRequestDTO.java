package com.fooddelivery.common.dto.identity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleRequestDTO {
    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[A-Za-z0-9_\\-]+$")
    private String serviceName;
    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[A-Za-z0-9_]+$")
    private String roleName;
}
