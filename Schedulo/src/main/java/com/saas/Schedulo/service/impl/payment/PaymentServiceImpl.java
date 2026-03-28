package com.saas.Schedulo.service.impl.payment;

import com.saas.Schedulo.dto.request.payment.AddPaymentMethodRequest;
import com.saas.Schedulo.dto.request.payment.ProcessPaymentRequest;
import com.saas.Schedulo.dto.response.PagedResponse;
import com.saas.Schedulo.dto.response.payment.InvoiceResponse;
import com.saas.Schedulo.dto.response.payment.PaymentMethodResponse;
import com.saas.Schedulo.dto.response.payment.PaymentResponse;
import com.saas.Schedulo.entity.organization.Organization;
import com.saas.Schedulo.entity.payment.Invoice;
import com.saas.Schedulo.entity.payment.Payment;
import com.saas.Schedulo.entity.payment.PaymentMethod;
import com.saas.Schedulo.repository.organization.OrganizationRepository;
import com.saas.Schedulo.repository.payment.InvoiceRepository;
import com.saas.Schedulo.repository.payment.PaymentMethodRepository;
import com.saas.Schedulo.repository.payment.PaymentRepository;
import com.saas.Schedulo.service.payment.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final InvoiceRepository invoiceRepository;
    private final OrganizationRepository organizationRepository;

    @Value("${stripe.api-key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Override
    @Transactional
    public PaymentResponse processPayment(ProcessPaymentRequest request, UUID organizationId) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        Payment payment = Payment.builder()
                .organization(org)
                .amount(BigDecimal.valueOf(request.getAmount()))
                .currency(request.getCurrency().toUpperCase())
                .status(Payment.PaymentStatus.PROCESSING)
                .paymentType(Payment.PaymentType.ONE_TIME)
                .paymentGateway("stripe")
                .build();

        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(Math.round(request.getAmount() * 100)) // cents
                    .setCurrency(request.getCurrency().toLowerCase())
                    .setPaymentMethod(request.getPaymentMethodId())
                    .setConfirm(true)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                    .build()
                    )
                    .build();

            PaymentIntent intent = PaymentIntent.create(params);
            payment.setExternalPaymentId(intent.getId());

            if ("succeeded".equals(intent.getStatus())) {
                payment.setStatus(Payment.PaymentStatus.SUCCEEDED);
                payment.setPaidAt(LocalDateTime.now());
            } else {
                payment.setStatus(Payment.PaymentStatus.PENDING);
            }

        } catch (StripeException e) {
            log.error("Stripe payment failed: {}", e.getMessage());
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureReason(e.getMessage());
        }

        payment = paymentRepository.save(payment);

        // Create invoice for successful payments
        if (payment.getStatus() == Payment.PaymentStatus.SUCCEEDED) {
            createInvoiceForPayment(payment, org);
        }

        return mapToPaymentResponse(payment);
    }

    @Override
    public PaymentResponse getPaymentById(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return mapToPaymentResponse(payment);
    }

    @Override
    public PagedResponse<PaymentResponse> getPaymentHistory(UUID organizationId, Pageable pageable) {
        Page<Payment> page = paymentRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId, pageable);
        List<PaymentResponse> content = page.getContent().stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());

        return PagedResponse.<PaymentResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(UUID paymentId, Double amount) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != Payment.PaymentStatus.SUCCEEDED) {
            throw new RuntimeException("Only succeeded payments can be refunded");
        }

        try {
            com.stripe.param.RefundCreateParams refundParams = com.stripe.param.RefundCreateParams.builder()
                    .setPaymentIntent(payment.getExternalPaymentId())
                    .setAmount(Math.round(amount * 100))
                    .build();

            com.stripe.model.Refund.create(refundParams);

            BigDecimal refundAmount = BigDecimal.valueOf(amount);
            BigDecimal currentRefunded = payment.getRefundedAmount() != null ? payment.getRefundedAmount() : BigDecimal.ZERO;
            payment.setRefundedAmount(currentRefunded.add(refundAmount));
            payment.setRefundedAt(LocalDateTime.now());

            if (payment.getRefundedAmount().compareTo(payment.getAmount()) >= 0) {
                payment.setStatus(Payment.PaymentStatus.REFUNDED);
            } else {
                payment.setStatus(Payment.PaymentStatus.PARTIALLY_REFUNDED);
            }

            payment = paymentRepository.save(payment);
        } catch (StripeException e) {
            log.error("Stripe refund failed: {}", e.getMessage());
            throw new RuntimeException("Refund failed: " + e.getMessage());
        }

        return mapToPaymentResponse(payment);
    }

    @Override
    @Transactional
    public PaymentMethodResponse addPaymentMethod(AddPaymentMethodRequest request, UUID organizationId) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        PaymentMethod method = PaymentMethod.builder()
                .organization(org)
                .methodType(PaymentMethod.MethodType.CARD)
                .paymentGateway(request.getPaymentGateway())
                .externalMethodId(request.getToken())
                .isDefault(request.getSetAsDefault())
                .billingEmail(request.getBillingEmail())
                .build();

        // Try to fetch card details from Stripe
        try {
            com.stripe.model.PaymentMethod stripeMethod =
                    com.stripe.model.PaymentMethod.retrieve(request.getToken());
            if (stripeMethod.getCard() != null) {
                method.setLastFourDigits(stripeMethod.getCard().getLast4());
                method.setCardBrand(stripeMethod.getCard().getBrand());
                method.setExpiryMonth(stripeMethod.getCard().getExpMonth().intValue());
                method.setExpiryYear(stripeMethod.getCard().getExpYear().intValue());
            }
        } catch (StripeException e) {
            log.warn("Could not fetch Stripe payment method details: {}", e.getMessage());
        }

        if (Boolean.TRUE.equals(request.getSetAsDefault())) {
            paymentMethodRepository.clearDefaultForOrganization(organizationId, UUID.randomUUID());
        }

        method = paymentMethodRepository.save(method);
        return mapToPaymentMethodResponse(method);
    }

    @Override
    public List<PaymentMethodResponse> getPaymentMethods(UUID organizationId) {
        return paymentMethodRepository.findByOrganizationIdAndIsDeletedFalse(organizationId).stream()
                .map(this::mapToPaymentMethodResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deletePaymentMethod(UUID methodId) {
        PaymentMethod method = paymentMethodRepository.findById(methodId)
                .orElseThrow(() -> new RuntimeException("Payment method not found"));
        method.setIsDeleted(true);
        paymentMethodRepository.save(method);
    }

    @Override
    @Transactional
    public void setDefaultPaymentMethod(UUID organizationId, UUID methodId) {
        PaymentMethod method = paymentMethodRepository.findById(methodId)
                .orElseThrow(() -> new RuntimeException("Payment method not found"));

        paymentMethodRepository.clearDefaultForOrganization(organizationId, methodId);
        method.setIsDefault(true);
        paymentMethodRepository.save(method);
    }

    @Override
    public InvoiceResponse getInvoice(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        return mapToInvoiceResponse(invoice);
    }

    @Override
    public PagedResponse<InvoiceResponse> getInvoices(UUID organizationId, Pageable pageable) {
        Page<Invoice> page = invoiceRepository.findByOrganizationIdOrderByIssueDateDesc(organizationId, pageable);
        List<InvoiceResponse> content = page.getContent().stream()
                .map(this::mapToInvoiceResponse)
                .collect(Collectors.toList());

        return PagedResponse.<InvoiceResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    // ── Private helper methods ─────────────────────────────────

    private void createInvoiceForPayment(Payment payment, Organization org) {
        String prefix = "INV-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        int nextNum = invoiceRepository.findMaxInvoiceNumber(prefix) + 1;
        String invoiceNumber = prefix + "-" + String.format("%04d", nextNum);

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .organization(org)
                .payment(payment)
                .subtotal(payment.getAmount())
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .total(payment.getAmount())
                .currency(payment.getCurrency())
                .status(Invoice.InvoiceStatus.PAID)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now())
                .paidDate(LocalDate.now())
                .build();

        invoiceRepository.save(invoice);
    }

    private PaymentResponse mapToPaymentResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .organizationId(p.getOrganization().getId())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .status(p.getStatus().name())
                .paymentType(p.getPaymentType() != null ? p.getPaymentType().name() : null)
                .paymentGateway(p.getPaymentGateway())
                .externalPaymentId(p.getExternalPaymentId())
                .paymentMethodType(p.getPaymentMethodType())
                .lastFourDigits(p.getLastFourDigits())
                .cardBrand(p.getCardBrand())
                .paidAt(p.getPaidAt())
                .failureReason(p.getFailureReason())
                .refundedAmount(p.getRefundedAmount())
                .refundedAt(p.getRefundedAt())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private PaymentMethodResponse mapToPaymentMethodResponse(PaymentMethod m) {
        return PaymentMethodResponse.builder()
                .id(m.getId())
                .methodType(m.getMethodType().name())
                .paymentGateway(m.getPaymentGateway())
                .lastFourDigits(m.getLastFourDigits())
                .cardBrand(m.getCardBrand())
                .expiryMonth(m.getExpiryMonth())
                .expiryYear(m.getExpiryYear())
                .holderName(m.getHolderName())
                .isDefault(m.getIsDefault())
                .billingEmail(m.getBillingEmail())
                .isExpired(m.isExpired())
                .build();
    }

    private InvoiceResponse mapToInvoiceResponse(Invoice i) {
        return InvoiceResponse.builder()
                .id(i.getId())
                .invoiceNumber(i.getInvoiceNumber())
                .organizationId(i.getOrganization().getId())
                .subtotal(i.getSubtotal())
                .taxAmount(i.getTaxAmount())
                .discountAmount(i.getDiscountAmount())
                .total(i.getTotal())
                .currency(i.getCurrency())
                .status(i.getStatus().name())
                .issueDate(i.getIssueDate())
                .dueDate(i.getDueDate())
                .paidDate(i.getPaidDate())
                .billingAddress(i.getBillingAddress())
                .notes(i.getNotes())
                .pdfUrl(i.getPdfUrl())
                .createdAt(i.getCreatedAt())
                .build();
    }
}
