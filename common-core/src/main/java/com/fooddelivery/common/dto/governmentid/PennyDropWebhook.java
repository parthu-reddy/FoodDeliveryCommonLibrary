package com.fooddelivery.common.dto.governmentid;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.UUID;
import org.openapitools.jackson.nullable.JsonNullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * PennyDropWebhook
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-24T18:00:52.688144+05:30[Asia/Kolkata]", comments = "Generator version: 7.6.0")
public class PennyDropWebhook {

  private UUID brandId;

  private String beneficiaryName;

  private String status;

  private String registeredBrandName;

  public PennyDropWebhook brandId(UUID brandId) {
    this.brandId = brandId;
    return this;
  }

  /**
   * Get brandId
   * @return brandId
  */
  @Valid 
  @Schema(name = "brandId", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("brandId")
  public UUID getBrandId() {
    return brandId;
  }

  public void setBrandId(UUID brandId) {
    this.brandId = brandId;
  }

  public PennyDropWebhook beneficiaryName(String beneficiaryName) {
    this.beneficiaryName = beneficiaryName;
    return this;
  }

  /**
   * Get beneficiaryName
   * @return beneficiaryName
  */
  
  @Schema(name = "beneficiaryName", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("beneficiaryName")
  public String getBeneficiaryName() {
    return beneficiaryName;
  }

  public void setBeneficiaryName(String beneficiaryName) {
    this.beneficiaryName = beneficiaryName;
  }

  public PennyDropWebhook status(String status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
  */
  
  @Schema(name = "status", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public PennyDropWebhook registeredBrandName(String registeredBrandName) {
    this.registeredBrandName = registeredBrandName;
    return this;
  }

  /**
   * Get registeredBrandName
   * @return registeredBrandName
  */
  
  @Schema(name = "registeredBrandName", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("registeredBrandName")
  public String getRegisteredBrandName() {
    return registeredBrandName;
  }

  public void setRegisteredBrandName(String registeredBrandName) {
    this.registeredBrandName = registeredBrandName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PennyDropWebhook pennyDropWebhook = (PennyDropWebhook) o;
    return Objects.equals(this.brandId, pennyDropWebhook.brandId) &&
        Objects.equals(this.beneficiaryName, pennyDropWebhook.beneficiaryName) &&
        Objects.equals(this.status, pennyDropWebhook.status) &&
        Objects.equals(this.registeredBrandName, pennyDropWebhook.registeredBrandName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(brandId, beneficiaryName, status, registeredBrandName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PennyDropWebhook {\n");
    sb.append("    brandId: ").append(toIndentedString(brandId)).append("\n");
    sb.append("    beneficiaryName: ").append(toIndentedString(beneficiaryName)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    registeredBrandName: ").append(toIndentedString(registeredBrandName)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

