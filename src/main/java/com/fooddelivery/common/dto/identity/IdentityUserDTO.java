package com.fooddelivery.common.dto.identity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IdentityUserDTO {
    private UUID id;
    private String phoneNumber;
    private List<String> roles;


public static class IdentityUserDTOBuilder {
private UUID id;
private String phoneNumber;
private List<String> roles;

IdentityUserDTOBuilder() {
        }

        /**
         * @return {@code this}.
         */
public IdentityUserDTO.IdentityUserDTOBuilder id(final UUID id) {
            this.id = id;
            return this;
        }

        /**
         * @return {@code this}.
         */
public IdentityUserDTO.IdentityUserDTOBuilder phoneNumber(final String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        /**
         * @return {@code this}.
         */
public IdentityUserDTO.IdentityUserDTOBuilder roles(final List<String> roles) {
            this.roles = roles;
            return this;
        }

public IdentityUserDTO build() {
            return new IdentityUserDTO(this.id, this.phoneNumber, this.roles);
        }

        @java.lang.Override
public java.lang.String toString() {
            return "IdentityUserDTO.IdentityUserDTOBuilder(id=" + this.id + ", phoneNumber=" + this.phoneNumber + ", roles=" + this.roles + ")";
        }
    }

public static IdentityUserDTO.IdentityUserDTOBuilder builder() {
        return new IdentityUserDTO.IdentityUserDTOBuilder();
    }

public UUID getId() {
        return this.id;
    }

public String getPhoneNumber() {
        return this.phoneNumber;
    }

public List<String> getRoles() {
        return this.roles;
    }

public void setId(final UUID id) {
        this.id = id;
    }

public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

public void setRoles(final List<String> roles) {
        this.roles = roles;
    }

    @java.lang.Override
public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof IdentityUserDTO)) return false;
        final IdentityUserDTO other = (IdentityUserDTO) o;
        if (!other.canEqual((java.lang.Object) this)) return false;
        final java.lang.Object this$id = this.getId();
        final java.lang.Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final java.lang.Object this$phoneNumber = this.getPhoneNumber();
        final java.lang.Object other$phoneNumber = other.getPhoneNumber();
        if (this$phoneNumber == null ? other$phoneNumber != null : !this$phoneNumber.equals(other$phoneNumber)) return false;
        final java.lang.Object this$roles = this.getRoles();
        final java.lang.Object other$roles = other.getRoles();
        if (this$roles == null ? other$roles != null : !this$roles.equals(other$roles)) return false;
        return true;
    }

protected boolean canEqual(final java.lang.Object other) {
        return other instanceof IdentityUserDTO;
    }

    @java.lang.Override
public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final java.lang.Object $phoneNumber = this.getPhoneNumber();
        result = result * PRIME + ($phoneNumber == null ? 43 : $phoneNumber.hashCode());
        final java.lang.Object $roles = this.getRoles();
        result = result * PRIME + ($roles == null ? 43 : $roles.hashCode());
        return result;
    }

    @java.lang.Override
public java.lang.String toString() {
        return "IdentityUserDTO(id=" + this.getId() + ", phoneNumber=" + this.getPhoneNumber() + ", roles=" + this.getRoles() + ")";
    }

public IdentityUserDTO() {
    }

public IdentityUserDTO(final UUID id, final String phoneNumber, final List<String> roles) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.roles = roles;
    }
}
