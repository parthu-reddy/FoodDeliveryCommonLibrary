package com.fooddelivery.common.dto.wallet;

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
 * SortObject
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-24T18:00:52.107395+05:30[Asia/Kolkata]", comments = "Generator version: 7.6.0")
public class SortObject {

  private String direction;

  private String nullHandling;

  private Boolean ascending;

  private String property;

  private Boolean ignoreCase;

  public SortObject direction(String direction) {
    this.direction = direction;
    return this;
  }

  /**
   * Get direction
   * @return direction
  */
  
  @Schema(name = "direction", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("direction")
  public String getDirection() {
    return direction;
  }

  public void setDirection(String direction) {
    this.direction = direction;
  }

  public SortObject nullHandling(String nullHandling) {
    this.nullHandling = nullHandling;
    return this;
  }

  /**
   * Get nullHandling
   * @return nullHandling
  */
  
  @Schema(name = "nullHandling", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("nullHandling")
  public String getNullHandling() {
    return nullHandling;
  }

  public void setNullHandling(String nullHandling) {
    this.nullHandling = nullHandling;
  }

  public SortObject ascending(Boolean ascending) {
    this.ascending = ascending;
    return this;
  }

  /**
   * Get ascending
   * @return ascending
  */
  
  @Schema(name = "ascending", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("ascending")
  public Boolean getAscending() {
    return ascending;
  }

  public void setAscending(Boolean ascending) {
    this.ascending = ascending;
  }

  public SortObject property(String property) {
    this.property = property;
    return this;
  }

  /**
   * Get property
   * @return property
  */
  
  @Schema(name = "property", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("property")
  public String getProperty() {
    return property;
  }

  public void setProperty(String property) {
    this.property = property;
  }

  public SortObject ignoreCase(Boolean ignoreCase) {
    this.ignoreCase = ignoreCase;
    return this;
  }

  /**
   * Get ignoreCase
   * @return ignoreCase
  */
  
  @Schema(name = "ignoreCase", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("ignoreCase")
  public Boolean getIgnoreCase() {
    return ignoreCase;
  }

  public void setIgnoreCase(Boolean ignoreCase) {
    this.ignoreCase = ignoreCase;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SortObject sortObject = (SortObject) o;
    return Objects.equals(this.direction, sortObject.direction) &&
        Objects.equals(this.nullHandling, sortObject.nullHandling) &&
        Objects.equals(this.ascending, sortObject.ascending) &&
        Objects.equals(this.property, sortObject.property) &&
        Objects.equals(this.ignoreCase, sortObject.ignoreCase);
  }

  @Override
  public int hashCode() {
    return Objects.hash(direction, nullHandling, ascending, property, ignoreCase);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SortObject {\n");
    sb.append("    direction: ").append(toIndentedString(direction)).append("\n");
    sb.append("    nullHandling: ").append(toIndentedString(nullHandling)).append("\n");
    sb.append("    ascending: ").append(toIndentedString(ascending)).append("\n");
    sb.append("    property: ").append(toIndentedString(property)).append("\n");
    sb.append("    ignoreCase: ").append(toIndentedString(ignoreCase)).append("\n");
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

