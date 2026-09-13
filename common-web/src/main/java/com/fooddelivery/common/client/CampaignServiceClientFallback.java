package com.fooddelivery.common.client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.UUID;

@Component
public class CampaignServiceClientFallback implements CampaignServiceClient {

    @Override
    public ResponseEntity<Map<String, String>> getAdvertiserOwner(UUID campaignId) {
        return ResponseEntity.status(503).build();
    }
    
    @Override
    public ResponseEntity<Map<String, String>> getAdvertiserUserId(UUID advertiserId) {
        return ResponseEntity.status(503).build();
    }
}
