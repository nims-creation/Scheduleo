package com.saas.Schedulo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

/**
 * Adds industry-standard security response headers to every HTTP response.
 * These headers protect against common web vulnerabilities:
 * - XSS (Cross-Site Scripting)
 * - Clickjacking
 * - MIME sniffing
 * - Protocol downgrade attacks
 */
@Configuration
public class SecurityHeadersConfig {

    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilter() {
        FilterRegistrationBean<SecurityHeadersFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new SecurityHeadersFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

    public static class SecurityHeadersFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {

            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // Prevent browsers from MIME-sniffing a response away from the declared content-type
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");

            // Protect against clickjacking — only allow framing from the same origin
            httpResponse.setHeader("X-Frame-Options", "SAMEORIGIN");

            // Force HTTPS for 1 year (includeSubDomains)
            httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

            // Disable legacy XSS filter (modern browsers use CSP instead)
            httpResponse.setHeader("X-XSS-Protection", "0");

            // Referrer sent only on same-origin requests
            httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

            // Restrict what browser features this app can use
            httpResponse.setHeader("Permissions-Policy",
                    "camera=(), microphone=(), geolocation=(), payment=()");

            chain.doFilter(request, response);
        }
    }
}
