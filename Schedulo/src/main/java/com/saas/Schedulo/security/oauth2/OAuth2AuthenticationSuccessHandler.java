package com.saas.Schedulo.security.oauth2;

import com.saas.Schedulo.security.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        // Normally we'd extract the user, generate a token, and redirect
        // For now, generating a basic fallback if it fails cast
        String targetUrl = "http://localhost:3000/oauth2/redirect";
        
        try {
            String token = jwtTokenProvider.generateAccessToken(authentication);
            targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("token", token)
                    .build().toUriString();
        } catch (Exception e) {
            targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("error", "Failed to generate token")
                    .build().toUriString();
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
