package com.fooddelivery.common.dto.maps;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.openapitools.jackson.nullable.JsonNullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * DispatchOrderRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-24T18:00:52.496807+05:30[Asia/Kolkata]", comments = "Generator version: 7.6.0")
public class DispatchOrderRequest {

  private String cityId;

  private String restaurantCoords;

  public DispatchOrderRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public DispatchOrderRequest(String cityId, String restaurantCoords) {
    this.cityId = cityId;
    this.restaurantCoords = restaurantCoords;
  }

  public DispatchOrderRequest cityId(String cityId) {
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

  public DispatchOrderRequest restaurantCoords(String restaurantCoords) {
    this.restaurantCoords = restaurantCoords;
    return this;
  }

  /**
   * Get restaurantCoords
   * @return restaurantCoords
  */
  @NotNull @Pattern(regexp = "^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?),\\s*[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)$") @Size(min = 0, max = 50) 
  @Schema(name = "restaurantCoords", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("restaurantCoords")
  public String getRestaurantCoords() {
    return restaurantCoords;
  }

  public void setRestaurantCoords(String restaurantCoords) {
    this.restaurantCoords = restaurantCoords;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DispatchOrderRequest dispatchOrderRequest = (DispatchOrderRequest) o;
    return Objects.equals(this.cityId, dispatchOrderRequest.cityId) &&
        Objects.equals(this.restaurantCoords, dispatchOrderRequest.restaurantCoords);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cityId, restaurantCoords);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DispatchOrderRequest {\n");
    sb.append("    cityId: ").append(toIndentedString(cityId)).append("\n");
    sb.append("    restaurantCoords: ").append(toIndentedString(restaurantCoords)).append("\n");
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

