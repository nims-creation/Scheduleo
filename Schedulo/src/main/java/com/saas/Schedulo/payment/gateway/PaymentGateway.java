package com.saas.Schedulo.payment.gateway;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentGateway {

    String getName();

    PaymentResult createPayment(PaymentRequest request);

    PaymentResult confirmPayment(String paymentId);

    PaymentResult refundPayment(String paymentId, BigDecimal amount);

    SubscriptionResult createSubscription(SubscriptionRequest request);

    SubscriptionResult cancelSubscription(String subscriptionId, boolean immediate);

    SubscriptionResult updateSubscription(String subscriptionId, UpdateSubscriptionRequest request);

    CustomerResult createCustomer(CustomerRequest request);

    PaymentMethodResult attachPaymentMethod(String customerId, String paymentMethodToken);

    PaymentMethodResult detachPaymentMethod(String paymentMethodId);

    WebhookResult handleWebhook(String payload, String signature);

    record PaymentRequest(
            String customerId,
            BigDecimal amount,
            String currency,
            String paymentMethodId,
            String description,
            Map<String, String> metadata
    ) {}

    record PaymentResult(
            boolean success,
            String paymentId,
            String status,
            String errorCode,
            String errorMessage
    ) {}

    record SubscriptionRequest(
            String customerId,
            String priceId,
            String paymentMethodId,
            Integer trialDays,
            Map<String, String> metadata
    ) {}

    record SubscriptionResult(
            boolean success,
            String subscriptionId,
            String status,
            String currentPeriodEnd,
            String errorCode,
            String errorMessage
    ) {}

    record UpdateSubscriptionRequest(
            String newPriceId,
            Boolean cancelAtPeriodEnd
    ) {}

    record CustomerRequest(
            String email,
            String name,
            Map<String, String> metadata
    ) {}

    record CustomerResult(
            boolean success,
            String customerId,
            String errorCode,
            String errorMessage
    ) {}

    record PaymentMethodResult(
            boolean success,
            String paymentMethodId,
            String type,
            String last4,
            String brand,
            Integer expMonth,
            Integer expYear,
            String errorCode,
            String errorMessage
    ) {}

    record WebhookResult(
            boolean success,
            String eventType,
            String resourceId,
            Map<String, Object> data,
            String errorMessage
    ) {}
}

