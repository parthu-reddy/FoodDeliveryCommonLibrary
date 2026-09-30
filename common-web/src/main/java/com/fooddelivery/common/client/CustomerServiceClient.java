package com.fooddelivery.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import jakarta.validation.Valid;
import java.util.List;

import com.fooddelivery.common.dto.ApiResponse;
import com.fooddelivery.common.dto.order.OrderReviewContextDto;
import com.fooddelivery.common.dto.order.OrderReviewAuthorizationRequest;
import com.fooddelivery.common.dto.order.OrderReviewAuthorizationResult;
import com.fooddelivery.common.dto.order.OrderChatParticipantDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "customer-service", contextId = "commonCustomerServiceClient")
public interface CustomerServiceClient {

    @GetMapping("/api/v1/internal/orders/{orderId}/participants")
    ResponseEntity<List<String>> getOrderParticipants(
            @PathVariable("orderId") String orderId,
            @RequestHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_CALLING_SERVICE) String serviceName);

    /**
     * Canonical chat roster sourced from the order aggregate. Consumers must not derive this
     * roster from a browser-provided participant list.
     */
    @GetMapping("/api/v1/internal/orders/{orderId}/chat-participants")
    ResponseEntity<List<OrderChatParticipantDto>> getOrderChatParticipants(
            @PathVariable("orderId") String orderId,
            @RequestHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_CALLING_SERVICE) String serviceName);

    /** Review context fetched by ReviewsService over a signed SERVICE identity. */
    @GetMapping("/api/v1/internal/orders/{orderId}/review-context")
    ResponseEntity<ApiResponse<OrderReviewContextDto>> getOrderReviewContext(
            @PathVariable("orderId") String orderId,
            @RequestHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_CALLING_SERVICE) String serviceName);

    /**
     * Checks each requested target against the order and authenticated participant role in one
     * call. The review service is trusted to provide the verified actor identity; the order service
     * remains authoritative for order membership and target relationships.
     */
    @PostMapping("/api/v1/internal/orders/{orderId}/review-authorizations")
    ResponseEntity<ApiResponse<java.util.List<OrderReviewAuthorizationResult>>> authorizeOrderReviewTargets(
            @PathVariable("orderId") String orderId,
            @Valid @RequestBody OrderReviewAuthorizationRequest request,
            @RequestHeader(com.fooddelivery.common.constants.HeaderConstants.HEADER_CALLING_SERVICE) String serviceName);
}
