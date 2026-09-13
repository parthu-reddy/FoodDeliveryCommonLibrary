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
 * BankRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-24T18:00:52.688144+05:30[Asia/Kolkata]", comments = "Generator version: 7.6.0")
public class BankRequest {

  private String accountNumber;

  private String ifscCode;

  private String kycFullName;

  public BankRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public BankRequest(String accountNumber, String ifscCode, String kycFullName) {
    this.accountNumber = accountNumber;
    this.ifscCode = ifscCode;
    this.kycFullName = kycFullName;
  }

  public BankRequest accountNumber(String accountNumber) {
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

  public BankRequest ifscCode(String ifscCode) {
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

  public BankRequest kycFullName(String kycFullName) {
    this.kycFullName = kycFullName;
    return this;
  }

  /**
   * Get kycFullName
   * @return kycFullName
  */
  @NotNull 
  @Schema(name = "kycFullName", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("kycFullName")
  public String getKycFullName() {
    return kycFullName;
  }

  public void setKycFullName(String kycFullName) {
    this.kycFullName = kycFullName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BankRequest bankRequest = (BankRequest) o;
    return Objects.equals(this.accountNumber, bankRequest.accountNumber) &&
        Objects.equals(this.ifscCode, bankRequest.ifscCode) &&
        Objects.equals(this.kycFullName, bankRequest.kycFullName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accountNumber, ifscCode, kycFullName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BankRequest {\n");
    sb.append("    accountNumber: ").append(toIndentedString(accountNumber)).append("\n");
    sb.append("    ifscCode: ").append(toIndentedString(ifscCode)).append("\n");
    sb.append("    kycFullName: ").append(toIndentedString(kycFullName)).append("\n");
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

