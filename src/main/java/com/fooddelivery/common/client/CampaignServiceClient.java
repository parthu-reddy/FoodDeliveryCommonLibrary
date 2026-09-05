package com.fooddelivery.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "campaign-service", fallback = CampaignServiceClientFallback.class)
public interface CampaignServiceClient {

    @GetMapping("/api/v1/internal/campaigns/{campaignId}/advertiser")
    ResponseEntity<Map<String, String>> getAdvertiserOwner(@PathVariable("campaignId") UUID campaignId);
    
    @GetMapping("/api/v1/internal/advertisers/{advertiserId}/owner")
    ResponseEntity<Map<String, String>> getAdvertiserUserId(@PathVariable("advertiserId") UUID advertiserId);
}
