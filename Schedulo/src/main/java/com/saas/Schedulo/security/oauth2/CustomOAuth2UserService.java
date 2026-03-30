package com.saas.Schedulo.security.oauth2;

import com.saas.Schedulo.entity.user.Role;
import com.saas.Schedulo.entity.user.User;
import com.saas.Schedulo.repository.user.RoleRepository;
import com.saas.Schedulo.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // Fetch user from OAuth2 provider
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String provider = userRequest.getClientRegistration().getRegistrationId();
        
        // Extract attributes
        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }
        
        Optional<User> userOptional = userRepository.findByEmailIgnoreCase(email);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Upgrading the auth provider if they signed up locally first
            if (user.getAuthProvider() == User.AuthProvider.LOCAL) {
                try {
                    user.setAuthProvider(User.AuthProvider.valueOf(provider.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    user.setAuthProvider(User.AuthProvider.GOOGLE);
                }
                user.setProviderId(oAuth2User.getName());
                userRepository.save(user);
            }
        } else {
            // Register new user
            user = new User();
            user.setEmail(email);
            
            // Generate a random password as they log in via OAuth
            user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            
            // Parsing name
            String name = oAuth2User.getAttribute("name");
            if (name != null && name.contains(" ")) {
                user.setFirstName(name.substring(0, name.indexOf(" ")));
                user.setLastName(name.substring(name.indexOf(" ") + 1));
            } else if (name != null) {
                user.setFirstName(name);
                user.setLastName("");
            } else {
                String givenName = oAuth2User.getAttribute("given_name");
                String familyName = oAuth2User.getAttribute("family_name");
                user.setFirstName(givenName != null ? givenName : "User");
                user.setLastName(familyName != null ? familyName : "");
            }
            
            user.setProfileImageUrl(oAuth2User.getAttribute("picture"));
            user.setEmailVerified(true);
            user.setIsActive(true);
            
            try {
                user.setAuthProvider(User.AuthProvider.valueOf(provider.toUpperCase()));
            } catch (IllegalArgumentException e) {
                user.setAuthProvider(User.AuthProvider.GOOGLE);
            }
            user.setProviderId(oAuth2User.getName());
            
            Role defaultRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Default role not found"));
            user.getRoles().add(defaultRole);
            
            user = userRepository.save(user);
        }
        
        return new CustomOAuth2User(oAuth2User, user);
    }
}
