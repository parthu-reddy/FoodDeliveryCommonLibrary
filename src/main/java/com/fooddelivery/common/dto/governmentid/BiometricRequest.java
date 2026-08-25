package com.fooddelivery.common.dto.governmentid;

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
 * BiometricRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-24T18:00:52.688144+05:30[Asia/Kolkata]", comments = "Generator version: 7.6.0")
public class BiometricRequest {

  private String selfieUrl;

  public BiometricRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public BiometricRequest(String selfieUrl) {
    this.selfieUrl = selfieUrl;
  }

  public BiometricRequest selfieUrl(String selfieUrl) {
    this.selfieUrl = selfieUrl;
    return this;
  }

  /**
   * Get selfieUrl
   * @return selfieUrl
  */
  @NotNull 
  @Schema(name = "selfieUrl", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("selfieUrl")
  public String getSelfieUrl() {
    return selfieUrl;
  }

  public void setSelfieUrl(String selfieUrl) {
    this.selfieUrl = selfieUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BiometricRequest biometricRequest = (BiometricRequest) o;
    return Objects.equals(this.selfieUrl, biometricRequest.selfieUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(selfieUrl);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BiometricRequest {\n");
    sb.append("    selfieUrl: ").append(toIndentedString(selfieUrl)).append("\n");
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

