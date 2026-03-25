package com.saas.Schedulo.dto.request.payment;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessPaymentRequest {
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;
    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3)
    private String currency;
    @NotBlank(message = "Payment method is required")
    private String paymentMethodId;
    private String description;
    private String metadata;
}
