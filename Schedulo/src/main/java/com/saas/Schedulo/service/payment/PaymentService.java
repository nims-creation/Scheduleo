package com.saas.Schedulo.service.payment;

import com.saas.Schedulo.dto.request.payment.AddPaymentMethodRequest;
import com.saas.Schedulo.dto.request.payment.ProcessPaymentRequest;
import com.saas.Schedulo.dto.response.PagedResponse;
import com.saas.Schedulo.dto.response.payment.PaymentResponse;

import org.springframework.data.domain.Pageable;
import java.util.UUID;

import java.util.List;
import com.saas.Schedulo.dto.response.payment.PaymentMethodResponse;
import com.saas.Schedulo.dto.response.payment.InvoiceResponse;

public interface PaymentService {
    PaymentResponse processPayment(ProcessPaymentRequest request, UUID organizationId);
    PaymentResponse getPaymentById(UUID paymentId);
    PagedResponse<PaymentResponse> getPaymentHistory(UUID organizationId, Pageable pageable);
    PaymentResponse refundPayment(UUID paymentId, Double amount);
    PaymentMethodResponse addPaymentMethod(AddPaymentMethodRequest request, UUID organizationId);
    List<PaymentMethodResponse> getPaymentMethods(UUID organizationId);
    void deletePaymentMethod(UUID methodId);
    void setDefaultPaymentMethod(UUID organizationId, UUID methodId);
    InvoiceResponse getInvoice(UUID invoiceId);
    PagedResponse<InvoiceResponse> getInvoices(UUID organizationId, Pageable pageable);
}
