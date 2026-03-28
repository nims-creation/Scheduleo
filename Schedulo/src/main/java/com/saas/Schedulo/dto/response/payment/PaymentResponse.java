package com.saas.Schedulo.dto.response.payment;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private UUID id;
    private UUID organizationId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentType;
    private String paymentGateway;
    private String externalPaymentId;
    private String paymentMethodType;
    private String lastFourDigits;
    private String cardBrand;
    private LocalDateTime paidAt;
    private String failureReason;
    private BigDecimal refundedAmount;
    private LocalDateTime refundedAt;
    private LocalDateTime createdAt;
}
