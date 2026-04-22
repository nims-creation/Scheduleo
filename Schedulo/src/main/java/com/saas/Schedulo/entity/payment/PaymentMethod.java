package com.saas.Schedulo.entity.payment;

import com.saas.Schedulo.entity.base.BaseEntity;
import com.saas.Schedulo.entity.organization.Organization;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_methods", indexes = {
        @Index(name = "idx_payment_method_org", columnList = "organization_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethod extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Enumerated(EnumType.STRING)
    @Column(name = "method_type", nullable = false)
    private MethodType methodType;

    @Column(name = "payment_gateway", nullable = false, length = 50)
    private String paymentGateway;

    @Column(name = "external_method_id", nullable = false)
    private String externalMethodId;

    @Column(name = "last_four_digits", length = 4)
    private String lastFourDigits;

    @Column(name = "card_brand", length = 20)
    private String cardBrand;

    @Column(name = "expiry_month")
    private Integer expiryMonth;

    @Column(name = "expiry_year")
    private Integer expiryYear;

    @Column(name = "holder_name")
    private String holderName;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "billing_email")
    private String billingEmail;

    public enum MethodType {
        CARD, BANK_ACCOUNT, PAYPAL, UPI, WALLET
    }

    public boolean isExpired() {
        if (expiryMonth == null || expiryYear == null) return false;
        java.time.YearMonth expiry = java.time.YearMonth.of(expiryYear, expiryMonth);
        return java.time.YearMonth.now().isAfter(expiry);
    }
}

