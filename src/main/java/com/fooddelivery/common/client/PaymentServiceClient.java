package com.fooddelivery.common.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import com.fooddelivery.common.dto.payment.CreateOrderRequest;
import com.fooddelivery.common.dto.payment.RefundRequest;

@FeignClient(name = "payment-service", fallback = PaymentServiceClientFallback.class)
public interface PaymentServiceClient {

    @PostMapping("/api/v1/payments/create-order")
    String createOrder(@RequestParam("gateway") String gateway, @RequestBody CreateOrderRequest request);

    @PostMapping("/api/v1/payments/refund")
    String refundOrder(@RequestParam("gateway") String gateway, @RequestBody RefundRequest request);

    @GetMapping("/api/v1/payments/status")
    Map<String, Object> getPaymentStatus(@RequestParam("orderId") Long orderId);
}
