package com.fooddelivery.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/**
 * AD_CREATIVE_PENDING / AD_CREATIVE_APPROVED / AD_CREATIVE_REJECTED on {@code ad-events}.
 *
 * <p>Until A3 these events carried the raw AdCreative entity while their contracts claimed a
 * CampaignChangedEvent; nothing consumed them, so nothing noticed. This is the one shape now, written
 * by CampaignService's creative services and pinned by its producer contracts. A creative's review
 * changes what a campaign may serve; BiddingEngine learns the served asset from the campaign event.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AdCreativeChangedEvent(
        @NotNull UUID creativeId,
        @NotNull UUID campaignId,
        @NotNull UUID advertiserId,
        /** PENDING, APPROVED or REJECTED. */
        @NotNull String auditStatus,
        @Size(max = 500) String rejectionReason,
        @NotNull Instant occurredAt) { }
