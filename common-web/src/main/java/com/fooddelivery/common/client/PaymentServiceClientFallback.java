package com.fooddelivery.common.client;

import org.springframework.stereotype.Component;

import java.util.Map;
import com.fooddelivery.common.dto.payment.CreateOrderRequest;
import com.fooddelivery.common.dto.payment.CreatePaymentResponse;
import com.fooddelivery.common.dto.payment.RefundRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component("commonPaymentServiceClientFallback")
@lombok.RequiredArgsConstructor
public class PaymentServiceClientFallback implements PaymentServiceClient {

    @Override
    public CreatePaymentResponse createOrder(CreateOrderRequest request) {
        log.error("PaymentService is unreachable during createOrder for internalOrderId={}", request.getInternalOrderId());
        throw new IllegalStateException("PaymentService is down. Cannot create payment intent.");
    }

    @Override
    public String refundOrder(String gateway, RefundRequest request) {
        log.error("PaymentService is unreachable during refundOrder for orderId={}", request.getGatewayOrderId());
        throw new IllegalStateException("PaymentService is down. Cannot process refund.");
    }
}
