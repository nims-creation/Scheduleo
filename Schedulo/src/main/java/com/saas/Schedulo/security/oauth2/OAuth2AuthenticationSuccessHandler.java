package com.saas.Schedulo.security.oauth2;

import com.saas.Schedulo.security.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        String targetUrl = frontendUrl + "/oauth2/redirect";
        
        try {
            CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
            String token = jwtTokenProvider.generateAccessToken(oauth2User.getUser());
            
            targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("token", token)
                    .build().toUriString();
                    
            log.info("OAuth2 login successful for user: {}", oauth2User.getEmail());
        } catch (Exception e) {
            log.error("Error generating OAuth2 token", e);
            targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("error", "authentication_error")
                    .build().toUriString();
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
