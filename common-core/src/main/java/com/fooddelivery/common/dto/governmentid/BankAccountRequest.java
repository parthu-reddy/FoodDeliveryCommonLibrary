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
 * BankAccountRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-24T18:00:52.688144+05:30[Asia/Kolkata]", comments = "Generator version: 7.6.0")
public class BankAccountRequest {

  private UUID brandId;

  private String accountNumber;

  private String ifscCode;

  private String brandName;

  public BankAccountRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public BankAccountRequest(String accountNumber, String ifscCode, String brandName) {
    this.accountNumber = accountNumber;
    this.ifscCode = ifscCode;
    this.brandName = brandName;
  }

  public BankAccountRequest brandId(UUID brandId) {
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

  public BankAccountRequest accountNumber(String accountNumber) {
    this.accountNumber = accountNumber;
    return this;
  }

  /**
   * Get accountNumber
   * @return accountNumber
  */
  @NotNull 
  @Schema(name = "accountNumber", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("accountNumber")
  public String getAccountNumber() {
    return accountNumber;
  }

  public void setAccountNumber(String accountNumber) {
    this.accountNumber = accountNumber;
  }

  public BankAccountRequest ifscCode(String ifscCode) {
    this.ifscCode = ifscCode;
    return this;
  }

  /**
   * Get ifscCode
   * @return ifscCode
  */
  @NotNull 
  @Schema(name = "ifscCode", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("ifscCode")
  public String getIfscCode() {
    return ifscCode;
  }

  public void setIfscCode(String ifscCode) {
    this.ifscCode = ifscCode;
  }

  public BankAccountRequest brandName(String brandName) {
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
    BankAccountRequest bankAccountRequest = (BankAccountRequest) o;
    return Objects.equals(this.brandId, bankAccountRequest.brandId) &&
        Objects.equals(this.accountNumber, bankAccountRequest.accountNumber) &&
        Objects.equals(this.ifscCode, bankAccountRequest.ifscCode) &&
        Objects.equals(this.brandName, bankAccountRequest.brandName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(brandId, accountNumber, ifscCode, brandName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BankAccountRequest {\n");
    sb.append("    brandId: ").append(toIndentedString(brandId)).append("\n");
    sb.append("    accountNumber: ").append(toIndentedString(accountNumber)).append("\n");
    sb.append("    ifscCode: ").append(toIndentedString(ifscCode)).append("\n");
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

