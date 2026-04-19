package com.saas.Schedulo.security;

import com.saas.Schedulo.entity.user.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class CustomUserDetails implements UserDetails {
    private final UUID id;
    private final String email;
    private final String password;
    private final UUID organizationId;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;
    private final boolean accountNonLocked;

    // Kept in memory so AuthServiceImpl can access the entity after authenticate()
    // without a second DB round-trip. Not serialized.
    private final transient User user;

    public CustomUserDetails(User user) {
        this.user = user;
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPasswordHash();
        this.organizationId = user.getOrganization() != null ? user.getOrganization().getId() : null;
        this.enabled = user.getIsActive() && user.getEmailVerified();
        this.accountNonLocked = !user.isAccountLocked();
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

        user.getRoles().forEach(role -> {
            grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));
            role.getPermissions().forEach(permission -> {
                grantedAuthorities.add(new SimpleGrantedAuthority(
                        permission.getResource() + ":" + permission.getAction().name()
                ));
            });
        });
        this.authorities = grantedAuthorities;
    }
    @Override
    public String getUsername() {
        return email;
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
