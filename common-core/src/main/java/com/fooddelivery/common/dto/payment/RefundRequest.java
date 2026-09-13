package com.fooddelivery.common.dto.payment;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.math.BigDecimal;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * RefundRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-24T18:00:52.586887+05:30[Asia/Kolkata]", comments = "Generator version: 7.6.0")
public class RefundRequest {

  private String gatewayOrderId;

  private BigDecimal amountInInr;

  private String reason;

  public RefundRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public RefundRequest(String gatewayOrderId, BigDecimal amountInInr) {
    this.gatewayOrderId = gatewayOrderId;
    this.amountInInr = amountInInr;
  }

  public RefundRequest gatewayOrderId(String gatewayOrderId) {
    this.gatewayOrderId = gatewayOrderId;
    return this;
  }

  /**
   * Get gatewayOrderId
   * @return gatewayOrderId
  */
  @NotNull 
  @Schema(name = "gatewayOrderId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("gatewayOrderId")
  public String getGatewayOrderId() {
    return gatewayOrderId;
  }

  public void setGatewayOrderId(String gatewayOrderId) {
    this.gatewayOrderId = gatewayOrderId;
  }

  public RefundRequest amountInInr(BigDecimal amountInInr) {
    this.amountInInr = amountInInr;
    return this;
  }

  /**
   * Get amountInInr
   * @return amountInInr
  */
  @NotNull @Valid 
  @Schema(name = "amountInInr", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("amountInInr")
  public BigDecimal getAmountInInr() {
    return amountInInr;
  }

  public void setAmountInInr(BigDecimal amountInInr) {
    this.amountInInr = amountInInr;
  }

  public RefundRequest reason(String reason) {
    this.reason = reason;
    return this;
  }

  /**
   * Get reason
   * @return reason
  */
  
  @Schema(name = "reason", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("reason")
  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RefundRequest refundRequest = (RefundRequest) o;
    return Objects.equals(this.gatewayOrderId, refundRequest.gatewayOrderId) &&
        Objects.equals(this.amountInInr, refundRequest.amountInInr) &&
        Objects.equals(this.reason, refundRequest.reason);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gatewayOrderId, amountInInr, reason);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RefundRequest {\n");
    sb.append("    gatewayOrderId: ").append(toIndentedString(gatewayOrderId)).append("\n");
    sb.append("    amountInInr: ").append(toIndentedString(amountInInr)).append("\n");
    sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
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

