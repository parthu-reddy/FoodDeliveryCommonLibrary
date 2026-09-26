package com.fooddelivery.common.dto.campaign;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CampaignPacingDTO {
    private Double dailyBudget;
    private Double lifetimeBudget;
    @jakarta.validation.constraints.NotNull
    private UUID advertiserId;
    /** The advertiser's IANA zone: the daily budget resets at midnight there. */
    @jakarta.validation.constraints.NotNull
    private String timeZone;
}
