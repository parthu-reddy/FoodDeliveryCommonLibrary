package com.fooddelivery.common.dto.governmentid;

import java.time.Instant;
import java.util.UUID;

/** Short-lived upload capability; never log or put this response in audit details. */
public record DocumentUploadDto(UUID documentId, String uploadUrl, String objectKey,
                                String contentType, long contentLength, Instant expiresAt) { }
