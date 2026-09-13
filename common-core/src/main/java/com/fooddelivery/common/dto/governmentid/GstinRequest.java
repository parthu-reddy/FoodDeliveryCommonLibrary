package com.fooddelivery.common.dto.governmentid;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.UUID;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * GstinRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-24T18:00:52.688144+05:30[Asia/Kolkata]", comments = "Generator version: 7.6.0")
public class GstinRequest {

  private UUID brandId;

  private String gstin;

  private String brandName;

  public GstinRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public GstinRequest(String gstin, String brandName) {
    this.gstin = gstin;
    this.brandName = brandName;
  }

  public GstinRequest brandId(UUID brandId) {
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

  public GstinRequest gstin(String gstin) {
    this.gstin = gstin;
    return this;
  }

  /**
   * Get gstin
   * @return gstin
  */
  @NotNull 
  @Schema(name = "gstin", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("gstin")
  public String getGstin() {
    return gstin;
  }

  public void setGstin(String gstin) {
    this.gstin = gstin;
  }

  public GstinRequest brandName(String brandName) {
    this.brandName = brandName;
    return this;
  }

  /**
   * Get brandName
   * @return brandName
  */
  @NotNull 
  @Schema(name = "brandName", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("brandName")
  public String getBrandName() {
    return brandName;
  }

  public void setBrandName(String brandName) {
    this.brandName = brandName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GstinRequest gstinRequest = (GstinRequest) o;
    return Objects.equals(this.brandId, gstinRequest.brandId) &&
        Objects.equals(this.gstin, gstinRequest.gstin) &&
        Objects.equals(this.brandName, gstinRequest.brandName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(brandId, gstin, brandName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GstinRequest {\n");
    sb.append("    brandId: ").append(toIndentedString(brandId)).append("\n");
    sb.append("    gstin: ").append(toIndentedString(gstin)).append("\n");
    sb.append("    brandName: ").append(toIndentedString(brandName)).append("\n");
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

