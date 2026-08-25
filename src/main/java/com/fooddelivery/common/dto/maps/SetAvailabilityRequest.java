package com.fooddelivery.common.dto.maps;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * SetAvailabilityRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-24T18:00:52.496807+05:30[Asia/Kolkata]", comments = "Generator version: 7.6.0")
public class SetAvailabilityRequest {

  private String cityId;

  private String driverId;

  private Boolean available;

  public SetAvailabilityRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public SetAvailabilityRequest(String cityId, String driverId, Boolean available) {
    this.cityId = cityId;
    this.driverId = driverId;
    this.available = available;
  }

  public SetAvailabilityRequest cityId(String cityId) {
    this.cityId = cityId;
    return this;
  }

  /**
   * Get cityId
   * @return cityId
  */
  @NotNull @Pattern(regexp = "^[A-Za-z0-9_\\-]+$") @Size(min = 0, max = 50) 
  @Schema(name = "cityId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("cityId")
  public String getCityId() {
    return cityId;
  }

  public void setCityId(String cityId) {
    this.cityId = cityId;
  }

  public SetAvailabilityRequest driverId(String driverId) {
    this.driverId = driverId;
    return this;
  }

  /**
   * Get driverId
   * @return driverId
  */
  @NotNull @Pattern(regexp = "^[0-9a-fA-F\\-]{36}$") @Size(min = 0, max = 36) 
  @Schema(name = "driverId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("driverId")
  public String getDriverId() {
    return driverId;
  }

  public void setDriverId(String driverId) {
    this.driverId = driverId;
  }

  public SetAvailabilityRequest available(Boolean available) {
    this.available = available;
    return this;
  }

  /**
   * Get available
   * @return available
  */
  @NotNull 
  @Schema(name = "available", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("available")
  public Boolean getAvailable() {
    return available;
  }

  public void setAvailable(Boolean available) {
    this.available = available;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SetAvailabilityRequest setAvailabilityRequest = (SetAvailabilityRequest) o;
    return Objects.equals(this.cityId, setAvailabilityRequest.cityId) &&
        Objects.equals(this.driverId, setAvailabilityRequest.driverId) &&
        Objects.equals(this.available, setAvailabilityRequest.available);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cityId, driverId, available);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SetAvailabilityRequest {\n");
    sb.append("    cityId: ").append(toIndentedString(cityId)).append("\n");
    sb.append("    driverId: ").append(toIndentedString(driverId)).append("\n");
    sb.append("    available: ").append(toIndentedString(available)).append("\n");
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

