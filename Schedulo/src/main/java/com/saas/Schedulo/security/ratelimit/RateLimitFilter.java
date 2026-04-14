package com.saas.Schedulo.security.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-IP rate limiting on sensitive auth endpoints using Bucket4j token-bucket algorithm.
 *
 * Rules (enforced per client IP):
 *   /api/v1/auth/login  — 5 requests per 15 minutes
 *   /api/v1/auth/signup — 3 requests per 60 minutes
 *
 * Buckets are stored in a local ConcurrentHashMap (process-level).
 * On a multi-instance deployment (horizontal scaling) swap the bucket store
 * for a Redis-backed Bucket4j distributed store (bucket4j-redis module).
 *
 * Returns HTTP 429 with Retry-After header and a structured JSON error body.
 */
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH  = "/api/v1/auth/login";
    private static final String SIGNUP_PATH = "/api/v1/auth/signup";

    // Login: 5 tokens, refill 5 every 15 minutes
    private static final int  LOGIN_CAPACITY      = 5;
    private static final long LOGIN_REFILL_MINUTES = 15;

    // Signup: 3 tokens, refill 3 every 60 minutes
    private static final int  SIGNUP_CAPACITY       = 3;
    private static final long SIGNUP_REFILL_MINUTES = 60;

    /**
     * Separate bucket stores for each endpoint so limits are independent.
     * Key = clientIp, Value = Bucket
     */
    private final Map<String, Bucket> loginBuckets  = new ConcurrentHashMap<>();
    private final Map<String, Bucket> signupBuckets = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Only intercept login and signup POST requests
        return !(path.equals(LOGIN_PATH) || path.equals(SIGNUP_PATH));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = resolveClientIp(request);
        String path     = request.getServletPath();

        Bucket bucket;
        long retryAfterSeconds;

        if (path.equals(LOGIN_PATH)) {
            bucket            = loginBuckets.computeIfAbsent(clientIp, k -> buildLoginBucket());
            retryAfterSeconds = Duration.ofMinutes(LOGIN_REFILL_MINUTES).toSeconds();
        } else {
            bucket            = signupBuckets.computeIfAbsent(clientIp, k -> buildSignupBucket());
            retryAfterSeconds = Duration.ofMinutes(SIGNUP_REFILL_MINUTES).toSeconds();
        }

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for IP {} on path {}", clientIp, path);
            sendRateLimitResponse(response, retryAfterSeconds);
        }
    }

    // ── Bucket factories ──────────────────────────────────────────────────────

    private Bucket buildLoginBucket() {
        Bandwidth limit = Bandwidth.classic(
                LOGIN_CAPACITY,
                Refill.intervally(LOGIN_CAPACITY, Duration.ofMinutes(LOGIN_REFILL_MINUTES))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket buildSignupBucket() {
        Bandwidth limit = Bandwidth.classic(
                SIGNUP_CAPACITY,
                Refill.intervally(SIGNUP_CAPACITY, Duration.ofMinutes(SIGNUP_REFILL_MINUTES))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Resolves the real client IP, honouring X-Forwarded-For from Render's proxy.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // X-Forwarded-For can be a comma-separated list; take the first entry
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void sendRateLimitResponse(HttpServletResponse response, long retryAfterSeconds)
            throws IOException {

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));

        Map<String, Object> body = Map.of(
                "success", false,
                "message", "Too many requests. Please try again later.",
                "error", Map.of(
                        "code",        "RATE_LIMIT_EXCEEDED",
                        "description", "You have exceeded the request limit for this endpoint. " +
                                       "Please wait " + retryAfterSeconds + " seconds before retrying.",
                        "retryAfter",  retryAfterSeconds
                )
        );

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
