package com.saas.Schedulo.exception.payment;


import com.saas.Schedulo.exception.base.TimetableException;
import org.springframework.http.HttpStatus;

public class PaymentFailedException extends TimetableException {
    public PaymentFailedException(String message) {
        super(message, "PAY_001", HttpStatus.PAYMENT_REQUIRED);
    }
    public PaymentFailedException(String message, Object details) {
        super(message, "PAY_001", HttpStatus.PAYMENT_REQUIRED, details);
    }
}
