package com.saas.Schedulo.exception.payment;

import com.saas.Schedulo.exception.base.TimetableException;
import org.springframework.http.HttpStatus;

public class SubscriptionExpiredException extends TimetableException {
    public SubscriptionExpiredException() {
        super("Subscription has expired", "PAY_002", HttpStatus.PAYMENT_REQUIRED);
    }
    public SubscriptionExpiredException(String message) {
        super(message, "PAY_002", HttpStatus.PAYMENT_REQUIRED);
    }
}
