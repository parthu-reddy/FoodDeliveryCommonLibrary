package com.fooddelivery.common.dto.payment;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.math.BigDecimal;
import org.openapitools.jackson.nullable.JsonNullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * CreateOrderRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-24T18:00:52.586887+05:30[Asia/Kolkata]", comments = "Generator version: 7.6.0")
public class CreateOrderRequest {

  private com.fooddelivery.common.enums.PaymentPurpose purpose;

  private String referenceId;

  private BigDecimal amountInInr;

  private String customerPhone;

  private com.fooddelivery.common.enums.PaymentMethod paymentMethod;

  public CreateOrderRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CreateOrderRequest(com.fooddelivery.common.enums.PaymentPurpose purpose, String referenceId, BigDecimal amountInInr) {
    this.purpose = purpose;
    this.referenceId = referenceId;
    this.amountInInr = amountInInr;
  }

  /** What this payment is for: the gateway chooses each outcome's event from it (DiningPickupBooking D48). */
  @NotNull
  @Schema(name = "purpose", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("purpose")
  public com.fooddelivery.common.enums.PaymentPurpose getPurpose() {
    return purpose;
  }

  public void setPurpose(com.fooddelivery.common.enums.PaymentPurpose purpose) {
    this.purpose = purpose;
  }

  /**
   * The caller's own id for what is being paid: an order id, a top-up id or a deposit's attempt reference. The gateway never
   * reads meaning from it. At most 50 characters: it is also the outbox aggregate id of every event about this payment.
   */
  @NotNull @Size(max = 50)
  @Schema(name = "referenceId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("referenceId")
  public String getReferenceId() {
    return referenceId;
  }

  public void setReferenceId(String referenceId) {
    this.referenceId = referenceId;
  }

  public CreateOrderRequest amountInInr(BigDecimal amountInInr) {
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

  public CreateOrderRequest customerPhone(String customerPhone) {
    this.customerPhone = customerPhone;
    return this;
  }

  /**
   * Get customerPhone
   * @return customerPhone
  */
  
  @Schema(name = "customerPhone", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("customerPhone")
  public String getCustomerPhone() {
    return customerPhone;
  }

  public void setCustomerPhone(String customerPhone) {
    this.customerPhone = customerPhone;
  }

  public CreateOrderRequest paymentMethod(com.fooddelivery.common.enums.PaymentMethod paymentMethod) {
    this.paymentMethod = paymentMethod;
    return this;
  }

  @NotNull
  @Schema(name = "paymentMethod", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("paymentMethod")
  public com.fooddelivery.common.enums.PaymentMethod getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(com.fooddelivery.common.enums.PaymentMethod paymentMethod) {
    this.paymentMethod = paymentMethod;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateOrderRequest createOrderRequest = (CreateOrderRequest) o;
    return this.purpose == createOrderRequest.purpose &&
        Objects.equals(this.referenceId, createOrderRequest.referenceId) &&
        Objects.equals(this.amountInInr, createOrderRequest.amountInInr) &&
        Objects.equals(this.customerPhone, createOrderRequest.customerPhone);
  }

  @Override
  public int hashCode() {
    return Objects.hash(purpose, referenceId, amountInInr, customerPhone);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateOrderRequest {\n");
    sb.append("    purpose: ").append(toIndentedString(purpose)).append("\n");
    sb.append("    referenceId: ").append(toIndentedString(referenceId)).append("\n");
    sb.append("    amountInInr: ").append(toIndentedString(amountInInr)).append("\n");
    sb.append("    customerPhone: ").append(toIndentedString(customerPhone)).append("\n");
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

