package com.fooddelivery.common.event;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignChangedEvent {
    private UUID campaignId;
    private UUID advertiserId;
    private String status;
    private BigDecimal maxBid;
    private BigDecimal budget;
    private Boolean budgetExhausted;
    private Double pacingMultiplier;
    private Integer schemaVersion;
    private com.fooddelivery.common.dto.targeting.TargetingSummary targeting;
    private String creativeFormat;
    private String creativeAssetUrl;
    private String creativeVastXml;
}
