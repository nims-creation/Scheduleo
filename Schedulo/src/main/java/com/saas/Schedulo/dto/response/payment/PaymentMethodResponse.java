package com.saas.Schedulo.dto.response.payment;

import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodResponse {
    private UUID id;
    private String methodType;
    private String paymentGateway;
    private String lastFourDigits;
    private String cardBrand;
    private Integer expiryMonth;
    private Integer expiryYear;
    private String holderName;
    private Boolean isDefault;
    private String billingEmail;
    private Boolean isExpired;
}
