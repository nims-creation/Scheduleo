package com.saas.Schedulo.entity.payment;

import com.timetable.entity.base.BaseEntity;
import com.timetable.entity.organization.Organization;
import com.timetable.entity.subscription.Subscription;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payment_org", columnList = "organization_id"),
        @Index(name = "idx_payment_status", columnList = "status"),
        @Index(name = "idx_payment_external", columnList = "external_payment_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @Column(name = "payment_gateway", nullable = false, length = 50)
    private String paymentGateway;

    @Column(name = "external_payment_id")
    private String externalPaymentId;

    @Column(name = "payment_method_type")
    private String paymentMethodType;

    @Column(name = "last_four_digits", length = 4)
    private String lastFourDigits;

    @Column(name = "card_brand", length = 20)
    private String cardBrand;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "refunded_amount", precision = 10, scale = 2)
    private BigDecimal refundedAmount;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;

    @OneToOne(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Invoice invoice;

    public enum PaymentStatus {
        PENDING, PROCESSING, SUCCEEDED, FAILED, CANCELLED, REFUNDED, PARTIALLY_REFUNDED
    }

    public enum PaymentType {
        SUBSCRIPTION, ONE_TIME, UPGRADE, ADDON
    }
}

