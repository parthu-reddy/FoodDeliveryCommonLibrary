package com.fooddelivery.common.dto.governmentid;

import java.time.Instant;

/** A delivery executive's check outcomes as reported by government-id-validation-service. */
public record VerificationSummary(boolean allDocsApproved, boolean bankApproved, String dlVehicleClass,
                                  boolean dlApproved, boolean rcApproved, Instant lastBiometricVerificationAt) { }
