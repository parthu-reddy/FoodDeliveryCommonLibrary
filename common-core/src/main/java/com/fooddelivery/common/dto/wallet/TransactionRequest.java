package com.fooddelivery.common.dto.wallet;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.math.BigDecimal;
import org.openapitools.jackson.nullable.JsonNullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import com.fooddelivery.common.enums.ChargeCategory;

import java.util.*;
import jakarta.annotation.Generated;

/**
 * TransactionRequest
 */
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-24T18:00:52.107395+05:30[Asia/Kolkata]", comments = "Generator version: 7.6.0")
public class TransactionRequest {

  @NotNull
  private BigDecimal amount;

  @NotNull
  private UUID referenceId;

  @NotNull
  private ChargeCategory category;

  private String description;

  public TransactionRequest amount(BigDecimal amount) {
    this.amount = amount;
    return this;
  }

  @Valid
  @Schema(name = "amount", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("amount")
  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public TransactionRequest referenceId(UUID referenceId) {
    this.referenceId = referenceId;
    return this;
  }

  @Valid
  @Schema(name = "referenceId", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("referenceId")
  public UUID getReferenceId() {
    return referenceId;
  }

  public void setReferenceId(UUID referenceId) {
    this.referenceId = referenceId;
  }

  public TransactionRequest category(ChargeCategory category) {
    this.category = category;
    return this;
  }

  @Valid
  @Schema(name = "category", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("category")
  public ChargeCategory getCategory() {
    return category;
  }

  public void setCategory(ChargeCategory category) {
    this.category = category;
  }

  public TransactionRequest description(String description) {
    this.description = description;
    return this;
  }

  @Schema(name = "description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TransactionRequest transactionRequest = (TransactionRequest) o;
    return Objects.equals(this.amount, transactionRequest.amount) &&
        Objects.equals(this.referenceId, transactionRequest.referenceId) &&
        Objects.equals(this.category, transactionRequest.category) &&
        Objects.equals(this.description, transactionRequest.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount, referenceId, category, description);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TransactionRequest {\n");
    sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
    sb.append("    referenceId: ").append(toIndentedString(referenceId)).append("\n");
    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
