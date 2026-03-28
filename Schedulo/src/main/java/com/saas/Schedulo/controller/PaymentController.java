package com.saas.Schedulo.controller;

import com.saas.Schedulo.dto.request.payment.AddPaymentMethodRequest;
import com.saas.Schedulo.dto.request.payment.ProcessPaymentRequest;
import com.saas.Schedulo.dto.response.ApiResponse;
import com.saas.Schedulo.dto.response.PagedResponse;
import com.saas.Schedulo.dto.response.payment.InvoiceResponse;
import com.saas.Schedulo.dto.response.payment.PaymentMethodResponse;
import com.saas.Schedulo.dto.response.payment.PaymentResponse;
import com.saas.Schedulo.security.CustomUserDetails;
import com.saas.Schedulo.security.annotation.CurrentUser;
import com.saas.Schedulo.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing and management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Process a payment", description = "Process a one-time payment via Stripe")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @Valid @RequestBody ProcessPaymentRequest request,
            @CurrentUser CustomUserDetails currentUser) {
        PaymentResponse response = paymentService.processPayment(request, currentUser.getOrganizationId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Payment processed successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(@PathVariable UUID id) {
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/history")
    @Operation(summary = "Get payment history", description = "Get paginated payment history for the organization")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<PagedResponse<PaymentResponse>>> getPaymentHistory(
            @CurrentUser CustomUserDetails currentUser,
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<PaymentResponse> response = paymentService.getPaymentHistory(currentUser.getOrganizationId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Refund a payment", description = "Issue a full or partial refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(
            @PathVariable UUID id,
            @RequestParam Double amount) {
        PaymentResponse response = paymentService.refundPayment(id, amount);
        return ResponseEntity.ok(ApiResponse.success(response, "Refund processed successfully"));
    }

    // ── Payment Methods ────────────────────────────────────────

    @PostMapping("/methods")
    @Operation(summary = "Add payment method", description = "Add a new payment method to the organization")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> addPaymentMethod(
            @Valid @RequestBody AddPaymentMethodRequest request,
            @CurrentUser CustomUserDetails currentUser) {
        PaymentMethodResponse response = paymentService.addPaymentMethod(request, currentUser.getOrganizationId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Payment method added successfully"));
    }

    @GetMapping("/methods")
    @Operation(summary = "Get payment methods", description = "List all payment methods for the organization")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<PaymentMethodResponse>>> getPaymentMethods(
            @CurrentUser CustomUserDetails currentUser) {
        List<PaymentMethodResponse> response = paymentService.getPaymentMethods(currentUser.getOrganizationId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/methods/{methodId}")
    @Operation(summary = "Delete payment method")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePaymentMethod(@PathVariable UUID methodId) {
        paymentService.deletePaymentMethod(methodId);
        return ResponseEntity.ok(ApiResponse.success(null, "Payment method removed"));
    }

    @PutMapping("/methods/{methodId}/default")
    @Operation(summary = "Set default payment method")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> setDefaultPaymentMethod(
            @PathVariable UUID methodId,
            @CurrentUser CustomUserDetails currentUser) {
        paymentService.setDefaultPaymentMethod(currentUser.getOrganizationId(), methodId);
        return ResponseEntity.ok(ApiResponse.success(null, "Default payment method updated"));
    }

    // ── Invoices ───────────────────────────────────────────────

    @GetMapping("/invoices/{invoiceId}")
    @Operation(summary = "Get invoice by ID")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoice(@PathVariable UUID invoiceId) {
        InvoiceResponse response = paymentService.getInvoice(invoiceId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/invoices")
    @Operation(summary = "Get invoices", description = "Get paginated invoices for the organization")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<PagedResponse<InvoiceResponse>>> getInvoices(
            @CurrentUser CustomUserDetails currentUser,
            @PageableDefault(size = 20) Pageable pageable) {
        PagedResponse<InvoiceResponse> response = paymentService.getInvoices(currentUser.getOrganizationId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
