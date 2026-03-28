package com.saas.Schedulo.dto.response.payment;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {
    private UUID id;
    private String invoiceNumber;
    private UUID organizationId;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal total;
    private String currency;
    private String status;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private String billingAddress;
    private String notes;
    private String pdfUrl;
    private LocalDateTime createdAt;
}
