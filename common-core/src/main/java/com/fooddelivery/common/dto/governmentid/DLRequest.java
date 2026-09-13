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
 * DLRequest
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-24T18:00:52.688144+05:30[Asia/Kolkata]", comments = "Generator version: 7.6.0")
public class DLRequest {

  private String dlNumber;

  private String dateOfBirth;

  private String documentUrl;

  public DLRequest() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public DLRequest(String dlNumber, String dateOfBirth) {
    this.dlNumber = dlNumber;
    this.dateOfBirth = dateOfBirth;
  }

  public DLRequest dlNumber(String dlNumber) {
    this.dlNumber = dlNumber;
    return this;
  }

  /**
   * Get dlNumber
   * @return dlNumber
  */
  @NotNull 
  @Schema(name = "dlNumber", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("dlNumber")
  public String getDlNumber() {
    return dlNumber;
  }

  public void setDlNumber(String dlNumber) {
    this.dlNumber = dlNumber;
  }

  public DLRequest dateOfBirth(String dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
    return this;
  }

  /**
   * Get dateOfBirth
   * @return dateOfBirth
  */
  @NotNull 
  @Schema(name = "dateOfBirth", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("dateOfBirth")
  public String getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(String dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public DLRequest documentUrl(String documentUrl) {
    this.documentUrl = documentUrl;
    return this;
  }

  /**
   * Get documentUrl
   * @return documentUrl
  */
  
  @Schema(name = "documentUrl", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("documentUrl")
  public String getDocumentUrl() {
    return documentUrl;
  }

  public void setDocumentUrl(String documentUrl) {
    this.documentUrl = documentUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DLRequest dlRequest = (DLRequest) o;
    return Objects.equals(this.dlNumber, dlRequest.dlNumber) &&
        Objects.equals(this.dateOfBirth, dlRequest.dateOfBirth) &&
        Objects.equals(this.documentUrl, dlRequest.documentUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dlNumber, dateOfBirth, documentUrl);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DLRequest {\n");
    sb.append("    dlNumber: ").append(toIndentedString(dlNumber)).append("\n");
    sb.append("    dateOfBirth: ").append(toIndentedString(dateOfBirth)).append("\n");
    sb.append("    documentUrl: ").append(toIndentedString(documentUrl)).append("\n");
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

