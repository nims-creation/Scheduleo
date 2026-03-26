package com.saas.Schedulo.security.oauth2;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Fetch user from OAuth2 provider
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        // TODO: Map the OAuth2User to our CustomUserDetails or User entity
        // For the scope of this chunk, we will return the base user. 
        // Actual mapping requires parsing attributes ("email", "name") based on provider.
        
        return oAuth2User;
    }
}
