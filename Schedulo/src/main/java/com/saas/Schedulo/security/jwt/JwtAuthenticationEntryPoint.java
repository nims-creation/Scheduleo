package com.saas.Schedulo.security.jwt;

import com.saas.Schedulo.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        ApiResponse.ErrorDetails errorDetails = ApiResponse.ErrorDetails.builder()
                .code("AUTH_001")
                .description("Authentication required to access this resource")
                .build();
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .success(false)
                .message("Unauthorized")
                .error(errorDetails)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .build();
        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }
}
