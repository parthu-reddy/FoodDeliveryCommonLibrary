package com.fooddelivery.common.dto.wallet;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fooddelivery.common.dto.wallet.PageableObject;
import com.fooddelivery.common.dto.wallet.SortObject;
import com.fooddelivery.common.dto.wallet.WalletTransaction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * PageWalletTransaction
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-08-24T18:00:52.107395+05:30[Asia/Kolkata]", comments = "Generator version: 7.6.0")
public class PageWalletTransaction {

  private Integer totalPages;

  private Long totalElements;

  private Integer size;

  @Valid
  private List<@Valid WalletTransaction> content = new ArrayList<>();

  private Integer number;

  @Valid
  private List<@Valid SortObject> sort = new ArrayList<>();

  private PageableObject pageable;

  private Boolean first;

  private Boolean last;

  private Integer numberOfElements;

  private Boolean empty;

  public PageWalletTransaction totalPages(Integer totalPages) {
    this.totalPages = totalPages;
    return this;
  }

  /**
   * Get totalPages
   * @return totalPages
  */
  
  @Schema(name = "totalPages", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalPages")
  public Integer getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(Integer totalPages) {
    this.totalPages = totalPages;
  }

  public PageWalletTransaction totalElements(Long totalElements) {
    this.totalElements = totalElements;
    return this;
  }

  /**
   * Get totalElements
   * @return totalElements
  */
  
  @Schema(name = "totalElements", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalElements")
  public Long getTotalElements() {
    return totalElements;
  }

  public void setTotalElements(Long totalElements) {
    this.totalElements = totalElements;
  }

  public PageWalletTransaction size(Integer size) {
    this.size = size;
    return this;
  }

  /**
   * Get size
   * @return size
  */
  
  @Schema(name = "size", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("size")
  public Integer getSize() {
    return size;
  }

  public void setSize(Integer size) {
    this.size = size;
  }

  public PageWalletTransaction content(List<@Valid WalletTransaction> content) {
    this.content = content;
    return this;
  }

  public PageWalletTransaction addContentItem(WalletTransaction contentItem) {
    if (this.content == null) {
      this.content = new ArrayList<>();
    }
    this.content.add(contentItem);
    return this;
  }

  /**
   * Get content
   * @return content
  */
  @Valid 
  @Schema(name = "content", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("content")
  public List<@Valid WalletTransaction> getContent() {
    return content;
  }

  public void setContent(List<@Valid WalletTransaction> content) {
    this.content = content;
  }

  public PageWalletTransaction number(Integer number) {
    this.number = number;
    return this;
  }

  /**
   * Get number
   * @return number
  */
  
  @Schema(name = "number", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("number")
  public Integer getNumber() {
    return number;
  }

  public void setNumber(Integer number) {
    this.number = number;
  }

  public PageWalletTransaction sort(List<@Valid SortObject> sort) {
    this.sort = sort;
    return this;
  }

  public PageWalletTransaction addSortItem(SortObject sortItem) {
    if (this.sort == null) {
      this.sort = new ArrayList<>();
    }
    this.sort.add(sortItem);
    return this;
  }

  /**
   * Get sort
   * @return sort
  */
  @Valid 
  @Schema(name = "sort", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("sort")
  public List<@Valid SortObject> getSort() {
    return sort;
  }

  public void setSort(List<@Valid SortObject> sort) {
    this.sort = sort;
  }

  public PageWalletTransaction pageable(PageableObject pageable) {
    this.pageable = pageable;
    return this;
  }

  /**
   * Get pageable
   * @return pageable
  */
  @Valid 
  @Schema(name = "pageable", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("pageable")
  public PageableObject getPageable() {
    return pageable;
  }

  public void setPageable(PageableObject pageable) {
    this.pageable = pageable;
  }

  public PageWalletTransaction first(Boolean first) {
    this.first = first;
    return this;
  }

  /**
   * Get first
   * @return first
  */
  
  @Schema(name = "first", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("first")
  public Boolean getFirst() {
    return first;
  }

  public void setFirst(Boolean first) {
    this.first = first;
  }

  public PageWalletTransaction last(Boolean last) {
    this.last = last;
    return this;
  }

  /**
   * Get last
   * @return last
  */
  
  @Schema(name = "last", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("last")


  public PageWalletTransaction numberOfElements(Integer numberOfElements) {
    this.numberOfElements = numberOfElements;
    return this;
  }

  /**
   * Get numberOfElements
   * @return numberOfElements
  */
  
  @Schema(name = "numberOfElements", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("numberOfElements")


  public PageWalletTransaction empty(Boolean empty) {
    this.empty = empty;
    return this;
  }

  /**
   * Get empty
   * @return empty
  */
  
  @Schema(name = "empty", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("empty")


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PageWalletTransaction pageWalletTransaction = (PageWalletTransaction) o;
    return Objects.equals(this.totalPages, pageWalletTransaction.totalPages) &&
        Objects.equals(this.totalElements, pageWalletTransaction.totalElements) &&
        Objects.equals(this.size, pageWalletTransaction.size) &&
        Objects.equals(this.content, pageWalletTransaction.content) &&
        Objects.equals(this.number, pageWalletTransaction.number) &&
        Objects.equals(this.sort, pageWalletTransaction.sort) &&
        Objects.equals(this.pageable, pageWalletTransaction.pageable) &&
        Objects.equals(this.first, pageWalletTransaction.first) &&
        Objects.equals(this.last, pageWalletTransaction.last) &&
        Objects.equals(this.numberOfElements, pageWalletTransaction.numberOfElements) &&
        Objects.equals(this.empty, pageWalletTransaction.empty);
  }

  @Override
  public int hashCode() {
    return Objects.hash(totalPages, totalElements, size, content, number, sort, pageable, first, last, numberOfElements, empty);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PageWalletTransaction {\n");
    sb.append("    totalPages: ").append(toIndentedString(totalPages)).append("\n");
    sb.append("    totalElements: ").append(toIndentedString(totalElements)).append("\n");
    sb.append("    size: ").append(toIndentedString(size)).append("\n");
    sb.append("    content: ").append(toIndentedString(content)).append("\n");
    sb.append("    number: ").append(toIndentedString(number)).append("\n");
    sb.append("    sort: ").append(toIndentedString(sort)).append("\n");
    sb.append("    pageable: ").append(toIndentedString(pageable)).append("\n");
    sb.append("    first: ").append(toIndentedString(first)).append("\n");
    sb.append("    last: ").append(toIndentedString(last)).append("\n");
    sb.append("    numberOfElements: ").append(toIndentedString(numberOfElements)).append("\n");
    sb.append("    empty: ").append(toIndentedString(empty)).append("\n");
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

