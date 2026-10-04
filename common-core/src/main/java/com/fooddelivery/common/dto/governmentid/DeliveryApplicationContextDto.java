package com.fooddelivery.common.dto.governmentid;
import com.fooddelivery.common.enums.ApplicationStatus;
import java.util.UUID;
/** Fresh state for document writes; contains no applicant details or provider data. */
public record DeliveryApplicationContextDto(UUID executiveId, ApplicationStatus status, Integer version) { }
