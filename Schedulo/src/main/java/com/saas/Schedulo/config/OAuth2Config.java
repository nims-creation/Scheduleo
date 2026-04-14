package com.saas.Schedulo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@Configuration
@ConditionalOnExpression(
    "!'${spring.security.oauth2.client.registration.google.client-id:}'.isBlank() && " +
    "!'${spring.security.oauth2.client.registration.google.client-id:}'.equalsIgnoreCase('disabled')"
)
public class OAuth2Config {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret:}")
    private String googleClientSecret;

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration google = CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId(googleClientId)
                .clientSecret(googleClientSecret == null ? "" : googleClientSecret)
                .build();

        return new InMemoryClientRegistrationRepository(google);
    }
}
