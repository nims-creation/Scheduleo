package com.saas.Schedulo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchClientAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(exclude = {
        ElasticsearchDataAutoConfiguration.class,
        ElasticsearchRepositoriesAutoConfiguration.class,
        ElasticsearchClientAutoConfiguration.class,
        ElasticsearchRestClientAutoConfiguration.class,
        // Exclude OAuth2 auto-config — we configure it manually in SecurityConfig
        // only when real GOOGLE_CLIENT_ID / GOOGLE_CLIENT_SECRET are provided
        OAuth2ClientAutoConfiguration.class
})
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
public class ScheduloApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScheduloApplication.class, args);
    }

}
