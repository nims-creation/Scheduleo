package com.saas.Schedulo.dto.request.payment;

import jakarta.validation.constraints.*;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddPaymentMethodRequest {
    @NotBlank(message = "Payment gateway is required")
    private String paymentGateway;
    @NotBlank(message = "Payment method token is required")
    private String token;
    @Builder.Default
    private Boolean setAsDefault = false;
    private String billingEmail;
}
