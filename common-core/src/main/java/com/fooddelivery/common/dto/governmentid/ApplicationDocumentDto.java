package com.fooddelivery.common.dto.governmentid;

import java.time.Instant;
import java.util.UUID;

/** Metadata only: downloading a KYC document requires a separate audited request. */
public record ApplicationDocumentDto(UUID documentId, String docType, String objectKey, String contentType,
                                      long contentLength, Instant uploadedAt) { }
