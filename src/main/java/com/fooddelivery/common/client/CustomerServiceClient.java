package com.fooddelivery.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.List;

@FeignClient(name = "customer-service", contextId = "commonCustomerServiceClient")
public interface CustomerServiceClient {

    @GetMapping("/api/v1/internal/orders/{orderId}/participants")
    ResponseEntity<List<String>> getOrderParticipants(
            @PathVariable("orderId") String orderId,
            @RequestHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_CALLING_SERVICE) String serviceName);
}
