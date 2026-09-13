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
 * UpdateLocationRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-24T18:00:52.496807+05:30[Asia/Kolkata]", comments = "Generator version: 7.6.0")
public class UpdateLocationRequest {

  private String cityId;

  private String driverId;

  private Double lat;

  private Double lng;

  public UpdateLocationRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public UpdateLocationRequest(String cityId, String driverId, Double lat, Double lng) {
    this.cityId = cityId;
    this.driverId = driverId;
    this.lat = lat;
    this.lng = lng;
  }

  public UpdateLocationRequest cityId(String cityId) {
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

  public UpdateLocationRequest driverId(String driverId) {
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

  public UpdateLocationRequest lat(Double lat) {
    this.lat = lat;
    return this;
  }

  /**
   * Get lat
   * @return lat
  */
  @NotNull 
  @Schema(name = "lat", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("lat")
  public Double getLat() {
    return lat;
  }

  public void setLat(Double lat) {
    this.lat = lat;
  }

  public UpdateLocationRequest lng(Double lng) {
    this.lng = lng;
    return this;
  }

  /**
   * Get lng
   * @return lng
  */
  @NotNull 
  @Schema(name = "lng", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("lng")
  public Double getLng() {
    return lng;
  }

  public void setLng(Double lng) {
    this.lng = lng;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UpdateLocationRequest updateLocationRequest = (UpdateLocationRequest) o;
    return Objects.equals(this.cityId, updateLocationRequest.cityId) &&
        Objects.equals(this.driverId, updateLocationRequest.driverId) &&
        Objects.equals(this.lat, updateLocationRequest.lat) &&
        Objects.equals(this.lng, updateLocationRequest.lng);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cityId, driverId, lat, lng);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UpdateLocationRequest {\n");
    sb.append("    cityId: ").append(toIndentedString(cityId)).append("\n");
    sb.append("    driverId: ").append(toIndentedString(driverId)).append("\n");
    sb.append("    lat: ").append(toIndentedString(lat)).append("\n");
    sb.append("    lng: ").append(toIndentedString(lng)).append("\n");
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

