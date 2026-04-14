package com.saas.Schedulo.security.ratelimit;

/**
 * Thrown when a client exceeds the configured rate limit for an endpoint.
 * Caught and translated to HTTP 429 by RateLimitFilter itself.
 */
public class RateLimitException extends RuntimeException {

    private final long retryAfterSeconds;

    public RateLimitException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
