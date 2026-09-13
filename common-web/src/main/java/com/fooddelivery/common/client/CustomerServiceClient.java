package com.fooddelivery.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.List;

import com.fooddelivery.common.dto.ApiResponse;
import com.fooddelivery.common.dto.order.OrderReviewContextDto;

@FeignClient(name = "customer-service", contextId = "commonCustomerServiceClient")
public interface CustomerServiceClient {

    @GetMapping("/api/v1/internal/orders/{orderId}/participants")
    ResponseEntity<List<String>> getOrderParticipants(
            @PathVariable("orderId") String orderId,
            @RequestHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_CALLING_SERVICE) String serviceName);

    /**
     * Everything needed to decide whether a review of this order is allowed.
     *
     * <p>Called inside the customer's own request, so the identity this carries is the customer's —
     * {@code FeignSecurityInterceptor} forwards it along with the gateway's original signature. The
     * endpoint admits {@code SERVICE}, {@code ADMIN}, or the order's own customer, so no privilege
     * escalation is needed and none is granted: a caller can only read the context of an order they
     * placed.
     */
    @GetMapping("/api/v1/internal/orders/{orderId}/review-context")
    ResponseEntity<ApiResponse<OrderReviewContextDto>> getOrderReviewContext(
            @PathVariable("orderId") String orderId,
            @RequestHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_CALLING_SERVICE) String serviceName);
}
