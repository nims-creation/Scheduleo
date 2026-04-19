package com.saas.Schedulo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Strength 10 = Spring Security default (~100ms/hash, 2^10 rounds).
        // Strength 12 (~1500ms/hash) caused 2-3 min auth delays on Render free tier.
        return new BCryptPasswordEncoder(10);
    }
}
