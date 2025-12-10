package com.oauth.client_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

/**
 * 🔥 TOKEN RESPONSE LOGGER
 * 
 * Mawa! Ee class Google nundi vachina TOKEN RESPONSE ni
 * console lo print chestundi - exact ga em vastundo choodachu!
 * 
 * Browser Network tab lo ee response kanipinchadu endukante
 * idi Server-to-Server call (Backend → Google), browser dwara kaadu.
 */
@Configuration
public class TokenResponseLoggerConfig {

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {

        // Default client ni wrap chestunam - daani response ni log chestam
        DefaultAuthorizationCodeTokenResponseClient defaultClient = new DefaultAuthorizationCodeTokenResponseClient();

        return request -> {

            System.out.println("\n");
            System.out.println("╔══════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║     🔥 GOOGLE TOKEN EXCHANGE - SERVER-TO-SERVER CALL                        ║");
            System.out.println("║     (Idi Browser Network tab lo kanipinchadu!)                              ║");
            System.out.println("╚══════════════════════════════════════════════════════════════════════════════╝");

            // 1. Log the REQUEST being sent to Google
            System.out.println("\n📤 STEP 1: REQUEST TO GOOGLE TOKEN ENDPOINT");
            System.out.println("────────────────────────────────────────────────────────────────────────────────");
            System.out.println("   🌐 URL: https://oauth2.googleapis.com/token");
            System.out.println("   📝 Method: POST");
            System.out.println("   📋 Content-Type: application/x-www-form-urlencoded");
            System.out.println("");
            System.out.println("   📦 REQUEST BODY (form data):");
            System.out.println("   ┌─────────────────────────────────────────────────────────────────────────┐");
            System.out.println("   │ grant_type      = authorization_code                                   │");
            System.out.println("   │ code            = " + truncate(request.getAuthorizationExchange()
                    .getAuthorizationResponse().getCode(), 30) + "...     │");
            System.out.println("   │ redirect_uri    = " + request.getClientRegistration().getRedirectUri() + " │");
            System.out.println(
                    "   │ client_id       = " + truncate(request.getClientRegistration().getClientId(), 25) + "... │");
            System.out.println("   │ client_secret   = ******** (SECRET - never log this fully!)            │");
            System.out.println("   └─────────────────────────────────────────────────────────────────────────┘");

            // 2. Make the actual call to Google
            System.out.println("\n⏳ Calling Google Token Endpoint...");

            OAuth2AccessTokenResponse tokenResponse = defaultClient.getTokenResponse(request);

            // 3. Log the RESPONSE from Google
            System.out.println("\n📥 STEP 2: RESPONSE FROM GOOGLE (Token Exchange Successful!)");
            System.out.println("────────────────────────────────────────────────────────────────────────────────");
            System.out.println("");
            System.out.println("   ✅ GOOGLE RESPONSE RECEIVED!");
            System.out.println("");
            System.out.println("   📦 RESPONSE DATA:");
            System.out.println("   ┌─────────────────────────────────────────────────────────────────────────┐");

            // Access Token
            String accessToken = tokenResponse.getAccessToken().getTokenValue();
            System.out.println("   │ 1️⃣ ACCESS TOKEN (Opaque - for Google APIs only)                        │");
            System.out.println("   │    Value: " + truncate(accessToken, 50) + "...     │");
            System.out.println("   │    Type:  " + tokenResponse.getAccessToken().getTokenType().getValue()
                    + "                                                        │");
            System.out.println("   │    Expires In: " +
                    (tokenResponse.getAccessToken().getExpiresAt() != null
                            ? java.time.Duration.between(java.time.Instant.now(),
                                    tokenResponse.getAccessToken().getExpiresAt()).getSeconds() + " seconds"
                            : "N/A")
                    +
                    "                                         │");
            System.out.println("   │    Scopes: " + tokenResponse.getAccessToken().getScopes() + "  │");
            System.out.println("   │                                                                         │");

            // ID Token (JWT)
            Object idToken = tokenResponse.getAdditionalParameters().get("id_token");
            if (idToken != null) {
                String idTokenStr = idToken.toString();
                System.out.println("   │ 2️⃣ ID TOKEN (JWT! - This is what we use for Resource Server)          │");
                System.out.println("   │    ⭐ THIS IS THE IMPORTANT ONE FOR OUR USE CASE!                     │");
                System.out.println("   │    Value: " + truncate(idTokenStr, 50) + "... │");
                System.out.println("   │    Format: header.payload.signature (JWT)                             │");

                // Decode JWT header to show it's a real JWT
                String[] parts = idTokenStr.split("\\.");
                if (parts.length == 3) {
                    System.out.println("   │    ✓ Valid JWT with 3 parts                                            │");
                    try {
                        String headerJson = new String(java.util.Base64.getUrlDecoder().decode(parts[0]));
                        System.out.println("   │    Header: " + truncate(headerJson, 45) + " │");
                    } catch (Exception e) {
                        // Ignore decode errors
                    }
                }
            } else {
                System.out.println("   │ 2️⃣ ID TOKEN: Not present in response                                   │");
            }

            System.out.println("   │                                                                         │");

            // Refresh Token
            if (tokenResponse.getRefreshToken() != null) {
                System.out.println("   │ 3️⃣ REFRESH TOKEN (for getting new tokens when access token expires)   │");
                System.out.println(
                        "   │    Value: " + truncate(tokenResponse.getRefreshToken().getTokenValue(), 40) + "... │");
            } else {
                System.out.println("   │ 3️⃣ REFRESH TOKEN: Not provided (need access_type=offline)             │");
            }

            System.out.println("   │                                                                         │");
            System.out.println("   │ 4️⃣ Additional Parameters:                                               │");
            tokenResponse.getAdditionalParameters().forEach((key, value) -> {
                if (!"id_token".equals(key)) {
                    System.out.println("   │    " + key + ": " + truncate(String.valueOf(value), 50) + " │");
                }
            });

            System.out.println("   └─────────────────────────────────────────────────────────────────────────┘");

            System.out.println("\n╔══════════════════════════════════════════════════════════════════════════════╗");
            System.out.println("║     ✅ TOKEN EXCHANGE COMPLETE - Tokens will be saved in HTTP Session       ║");
            System.out.println("╚══════════════════════════════════════════════════════════════════════════════╝\n");

            return tokenResponse;
        };
    }

    private String truncate(String value, int maxLength) {
        if (value == null)
            return "null";
        if (value.length() <= maxLength)
            return value;
        return value.substring(0, maxLength);
    }
}
