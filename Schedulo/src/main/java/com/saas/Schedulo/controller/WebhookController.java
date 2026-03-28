package com.saas.Schedulo.controller;

import com.saas.Schedulo.dto.response.ApiResponse;
import com.saas.Schedulo.entity.payment.Payment;
import com.saas.Schedulo.entity.subscription.Subscription;
import com.saas.Schedulo.repository.payment.PaymentRepository;
import com.saas.Schedulo.repository.payment.SubscriptionRepository;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Webhooks", description = "Webhook endpoints for payment providers")
public class WebhookController {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Value("${stripe.webhook-secret}")
    private String stripeWebhookSecret;

    @PostMapping("/stripe")
    @Operation(summary = "Handle Stripe webhook events")
    public ResponseEntity<ApiResponse<String>> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
        } catch (Exception e) {
            log.error("Stripe webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid signature", null));
        }

        log.info("Received Stripe event: {} ({})", event.getType(), event.getId());

        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;
        if (deserializer.getObject().isPresent()) {
            stripeObject = deserializer.getObject().get();
        }

        switch (event.getType()) {
            case "payment_intent.succeeded" -> handlePaymentSucceeded((PaymentIntent) stripeObject);
            case "payment_intent.payment_failed" -> handlePaymentFailed((PaymentIntent) stripeObject);
            case "customer.subscription.updated" -> handleSubscriptionUpdated(event);
            case "customer.subscription.deleted" -> handleSubscriptionCancelled(event);
            case "invoice.paid" -> handleInvoicePaid(event);
            default -> log.info("Unhandled Stripe event type: {}", event.getType());
        }

        return ResponseEntity.ok(ApiResponse.success("Webhook processed"));
    }

    private void handlePaymentSucceeded(PaymentIntent intent) {
        if (intent == null) return;
        log.info("Payment succeeded: {}", intent.getId());

        Optional<Payment> paymentOpt = paymentRepository.findByExternalPaymentId(intent.getId());
        paymentOpt.ifPresent(payment -> {
            payment.setStatus(Payment.PaymentStatus.SUCCEEDED);
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);
            log.info("Updated payment {} to SUCCEEDED", payment.getId());
        });
    }

    private void handlePaymentFailed(PaymentIntent intent) {
        if (intent == null) return;
        log.info("Payment failed: {}", intent.getId());

        Optional<Payment> paymentOpt = paymentRepository.findByExternalPaymentId(intent.getId());
        paymentOpt.ifPresent(payment -> {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureReason(intent.getLastPaymentError() != null
                    ? intent.getLastPaymentError().getMessage() : "Payment failed");
            paymentRepository.save(payment);
            log.info("Updated payment {} to FAILED", payment.getId());
        });
    }

    private void handleSubscriptionUpdated(Event event) {
        log.info("Subscription updated event: {}", event.getId());
        // Subscription status sync is handled by SubscriptionService
    }

    private void handleSubscriptionCancelled(Event event) {
        log.info("Subscription cancelled event: {}", event.getId());
    }

    private void handleInvoicePaid(Event event) {
        log.info("Invoice paid event: {}", event.getId());
    }
}
