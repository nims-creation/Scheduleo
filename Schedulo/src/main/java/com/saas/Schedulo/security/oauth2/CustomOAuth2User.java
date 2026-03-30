package com.saas.Schedulo.security.oauth2;

import com.saas.Schedulo.entity.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final OAuth2User oAuth2User;
    private final User user;

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        user.getRoles().forEach(role -> {
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));
            role.getPermissions().forEach(permission -> {
                grantedAuthorities.add(new SimpleGrantedAuthority(
                        permission.getResource() + ":" + permission.getAction().name()
                ));
            });
        });
        return grantedAuthorities;
    }

    @Override
    public String getName() {
        return user.getEmail(); // Use email as the primary name/principal
    }

    public String getEmail() {
        return user.getEmail();
    }
    
    public User getUser() {
        return user;
    }
}
