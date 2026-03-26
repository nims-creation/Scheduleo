package com.saas.Schedulo.payment.gateway.stripe;

import com.saas.Schedulo.payment.gateway.PaymentGateway;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import com.stripe.param.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class StripePaymentGateway implements PaymentGateway {

    @Value("${stripe.api-key}")
    private String apiKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
    }

    @Override
    public String getName() {
        return "stripe";
    }

    @Override
    public PaymentResult createPayment(PaymentRequest request) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setCustomer(request.customerId())
                    .setAmount(request.amount().multiply(BigDecimal.valueOf(100)).longValue())
                    .setCurrency(request.currency().toLowerCase())
                    .setPaymentMethod(request.paymentMethodId())
                    .setDescription(request.description())
                    .setConfirm(true)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(
                                            PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER
                                    )
                                    .build()
                    )
                    .putAllMetadata(request.metadata() != null ? request.metadata() : Map.of())
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            return new PaymentResult(
                    paymentIntent.getStatus().equals("succeeded"),
                    paymentIntent.getId(),
                    paymentIntent.getStatus(),
                    null,
                    null
            );

        } catch (StripeException e) {
            log.error("Stripe payment failed: {}", e.getMessage());
            return new PaymentResult(false, null, "failed", e.getCode(), e.getMessage());
        }
    }

    @Override
    public PaymentResult confirmPayment(String paymentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentId);
            paymentIntent = paymentIntent.confirm();

            return new PaymentResult(
                    paymentIntent.getStatus().equals("succeeded"),
                    paymentIntent.getId(),
                    paymentIntent.getStatus(),
                    null,
                    null
            );

        } catch (StripeException e) {
            log.error("Stripe payment confirmation failed: {}", e.getMessage());
            return new PaymentResult(false, paymentId, "failed", e.getCode(), e.getMessage());
        }
    }

    @Override
    public PaymentResult refundPayment(String paymentId, BigDecimal amount) {
        try {
            RefundCreateParams.Builder paramsBuilder = RefundCreateParams.builder()
                    .setPaymentIntent(paymentId);

            if (amount != null) {
                paramsBuilder.setAmount(amount.multiply(BigDecimal.valueOf(100)).longValue());
            }

            Refund refund = Refund.create(paramsBuilder.build());

            return new PaymentResult(
                    refund.getStatus().equals("succeeded"),
                    refund.getId(),
                    refund.getStatus(),
                    null,
                    null
            );

        } catch (StripeException e) {
            log.error("Stripe refund failed: {}", e.getMessage());
            return new PaymentResult(false, null, "failed", e.getCode(), e.getMessage());
        }
    }

    @Override
    public SubscriptionResult createSubscription(SubscriptionRequest request) {
        try {
            SubscriptionCreateParams.Builder paramsBuilder = SubscriptionCreateParams.builder()
                    .setCustomer(request.customerId())
                    .addItem(SubscriptionCreateParams.Item.builder()
                            .setPrice(request.priceId())
                            .build())
                    .setDefaultPaymentMethod(request.paymentMethodId())
                    .putAllMetadata(request.metadata() != null ? request.metadata() : Map.of());

            if (request.trialDays() != null && request.trialDays() > 0) {
                paramsBuilder.setTrialPeriodDays(request.trialDays().longValue());
            }

            Subscription subscription = Subscription.create(paramsBuilder.build());

            return new SubscriptionResult(
                    true,
                    subscription.getId(),
                    subscription.getStatus(),
                    Instant.ofEpochSecond(subscription.getCurrentPeriodEnd()).toString(),
                    null,
                    null
            );

        } catch (StripeException e) {
            log.error("Stripe subscription creation failed: {}", e.getMessage());
            return new SubscriptionResult(false, null, "failed", null, e.getCode(), e.getMessage());
        }
    }

    @Override
    public SubscriptionResult cancelSubscription(String subscriptionId, boolean immediate) {
        try {
            Subscription subscription = Subscription.retrieve(subscriptionId);

            if (immediate) {
                subscription = subscription.cancel();
            } else {
                SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                        .setCancelAtPeriodEnd(true)
                        .build();
                subscription = subscription.update(params);
            }

            return new SubscriptionResult(
                    true,
                    subscription.getId(),
                    subscription.getStatus(),
                    Instant.ofEpochSecond(subscription.getCurrentPeriodEnd()).toString(),
                    null,
                    null
            );

        } catch (StripeException e) {
            log.error("Stripe subscription cancellation failed: {}", e.getMessage());
            return new SubscriptionResult(false, subscriptionId, "failed", null, e.getCode(), e.getMessage());
        }
    }

    @Override
    public SubscriptionResult updateSubscription(String subscriptionId, UpdateSubscriptionRequest request) {
        try {
            Subscription subscription = Subscription.retrieve(subscriptionId);

            SubscriptionUpdateParams.Builder paramsBuilder = SubscriptionUpdateParams.builder();

            if (request.newPriceId() != null) {
                paramsBuilder.addItem(SubscriptionUpdateParams.Item.builder()
                        .setId(subscription.getItems().getData().get(0).getId())
                        .setPrice(request.newPriceId())
                        .build());
            }

            if (request.cancelAtPeriodEnd() != null) {
                paramsBuilder.setCancelAtPeriodEnd(request.cancelAtPeriodEnd());
            }

            subscription = subscription.update(paramsBuilder.build());

            return new SubscriptionResult(
                    true,
                    subscription.getId(),
                    subscription.getStatus(),
                    Instant.ofEpochSecond(subscription.getCurrentPeriodEnd()).toString(),
                    null,
                    null
            );

        } catch (StripeException e) {
            log.error("Stripe subscription update failed: {}", e.getMessage());
            return new SubscriptionResult(false, subscriptionId, "failed", null, e.getCode(), e.getMessage());
        }
    }

    @Override
    public CustomerResult createCustomer(CustomerRequest request) {
        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setEmail(request.email())
                    .setName(request.name())
                    .putAllMetadata(request.metadata() != null ? request.metadata() : Map.of())
                    .build();

            Customer customer = Customer.create(params);

            return new CustomerResult(true, customer.getId(), null, null);

        } catch (StripeException e) {
            log.error("Stripe customer creation failed: {}", e.getMessage());
            return new CustomerResult(false, null, e.getCode(), e.getMessage());
        }
    }

    @Override
    public PaymentMethodResult attachPaymentMethod(String customerId, String paymentMethodToken) {
        try {
            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodToken);

            PaymentMethodAttachParams params = PaymentMethodAttachParams.builder()
                    .setCustomer(customerId)
                    .build();

            paymentMethod = paymentMethod.attach(params);

            PaymentMethod.Card card = paymentMethod.getCard();

            return new PaymentMethodResult(
                    true,
                    paymentMethod.getId(),
                    paymentMethod.getType(),
                    card != null ? card.getLast4() : null,
                    card != null ? card.getBrand() : null,
                    card != null ? card.getExpMonth().intValue() : null,
                    card != null ? card.getExpYear().intValue() : null,
                    null,
                    null
            );

        } catch (StripeException e) {
            log.error("Stripe payment method attachment failed: {}", e.getMessage());
            return new PaymentMethodResult(false, null, null, null, null, null, null, e.getCode(), e.getMessage());
        }
    }

    @Override
    public PaymentMethodResult detachPaymentMethod(String paymentMethodId) {
        try {
            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
            paymentMethod = paymentMethod.detach();

            return new PaymentMethodResult(
                    true,
                    paymentMethod.getId(),
                    null, null, null, null, null, null, null
            );

        } catch (StripeException e) {
            log.error("Stripe payment method detachment failed: {}", e.getMessage());
            return new PaymentMethodResult(false, null, null, null, null, null, null, e.getCode(), e.getMessage());
        }
    }

    @Override
    public WebhookResult handleWebhook(String payload, String signature) {
        try {
            Event event = Webhook.constructEvent(payload, signature, webhookSecret);

            Map<String, Object> data = new HashMap<>();
            data.put("event", event);

            if (event.getDataObjectDeserializer().getObject().isPresent()) {
                StripeObject stripeObject = event.getDataObjectDeserializer().getObject().get();
                data.put("object", stripeObject);
            }

            String resourceId = null;
            if (data.get("object") instanceof PaymentIntent pi) {
                resourceId = pi.getId();
            } else if (data.get("object") instanceof Subscription sub) {
                resourceId = sub.getId();
            } else if (data.get("object") instanceof Invoice inv) {
                resourceId = inv.getId();
            }

            return new WebhookResult(true, event.getType(), resourceId, data, null);

        } catch (SignatureVerificationException e) {
            log.error("Stripe webhook signature verification failed: {}", e.getMessage());
            return new WebhookResult(false, null, null, null, "Invalid signature");
        }
    }
}

