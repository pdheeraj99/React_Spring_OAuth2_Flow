package com.example.inventoryservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // ⭐ Require specific scope for stock endpoints
                        .requestMatchers("/api/stock/**").hasAuthority("SCOPE_read:inventory")
                        .anyRequest().authenticated())
                // ⭐ Enable Resource Server with JWT validation
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {
                        }));

        return http.build();
    }
}
