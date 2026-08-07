package com.fooddelivery.common.dto.identity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

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

    @java.lang.SuppressWarnings("all")
    public String getServiceName() {
        return this.serviceName;
    }

    @java.lang.SuppressWarnings("all")
    public String getRoleName() {
        return this.roleName;
    }

    @java.lang.SuppressWarnings("all")
    public void setServiceName(final String serviceName) {
        this.serviceName = serviceName;
    }

    @java.lang.SuppressWarnings("all")
    public void setRoleName(final String roleName) {
        this.roleName = roleName;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof RoleRequestDTO)) return false;
        final RoleRequestDTO other = (RoleRequestDTO) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        final java.lang.Object this$serviceName = this.getServiceName();
        final java.lang.Object other$serviceName = other.getServiceName();
        if (this$serviceName == null ? other$serviceName != null : !this$serviceName.equals(other$serviceName)) return false;
        final java.lang.Object this$roleName = this.getRoleName();
        final java.lang.Object other$roleName = other.getRoleName();
        if (this$roleName == null ? other$roleName != null : !this$roleName.equals(other$roleName)) return false;
        return true;
    }

    @java.lang.SuppressWarnings("all")
    protected boolean canEqual(final java.lang.Object other) {
        return other instanceof RoleRequestDTO;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $serviceName = this.getServiceName();
        result = result * PRIME + ($serviceName == null ? 43 : $serviceName.hashCode());
        final java.lang.Object $roleName = this.getRoleName();
        result = result * PRIME + ($roleName == null ? 43 : $roleName.hashCode());
        return result;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("all")
    public java.lang.String toString() {
        return "RoleRequestDTO(serviceName=" + this.getServiceName() + ", roleName=" + this.getRoleName() + ")";
    }

    @java.lang.SuppressWarnings("all")
    public RoleRequestDTO() {
    }

    @java.lang.SuppressWarnings("all")
    public RoleRequestDTO(final String serviceName, final String roleName) {
        this.serviceName = serviceName;
        this.roleName = roleName;
    }
}
