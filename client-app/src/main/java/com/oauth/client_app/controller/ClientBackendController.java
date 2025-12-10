package com.oauth.client_app.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ClientBackendController {

    // ========== API ENDPOINTS FOR REACT ==========

    // Check if user is logged in (React calls this on load)
    @GetMapping("/api/user-status")
    public Map<String, Object> getUserStatus(@AuthenticationPrincipal OidcUser user) {
        Map<String, Object> response = new HashMap<>();
        if (user != null) {
            response.put("authenticated", true);
            response.put("name", user.getFullName());
            response.put("email", user.getEmail());
            response.put("picture", user.getPicture());
        } else {
            response.put("authenticated", false);
        }
        return response;
    }

    // Get user details as JSON
    @GetMapping("/api/user")
    public Map<String, Object> getUser(@AuthenticationPrincipal OidcUser user) {
        Map<String, Object> response = new HashMap<>();
        if (user != null) {
            response.put("name", user.getFullName());
            response.put("email", user.getEmail());
            response.put("picture", user.getPicture());
            response.put("subject", user.getSubject());
        }
        return response;
    }

    // Idi mana HOME page (for direct browser access)
    @GetMapping("/")
    public String home() {
        return "<h1>Welcome to Client Backend! 🚀</h1>" +
                "<p>Nuvvu inka login avvaledu. <a href='/oauth2/authorization/google'>Click here to Login with Google</a></p>"
                +
                "<p>Or visit <a href='http://localhost:5173'>React App</a></p>";
    }

    // Login Success ayyaka, mana Backend daggara User Details untayi
    // Ee endpoint call chesthe, aa details bayataki vastayi
    @GetMapping("/user-info")
    public String getUserInfo(@AuthenticationPrincipal OAuth2User principal) {
        // 'principal' ante Login ayina User object (Spring Security inject chestundi)
        if (principal == null) {
            return "User not logged in!";
        }

        // User name & Email extract cheddam
        String name = principal.getAttribute("name");
        String email = principal.getAttribute("email");

        return "<h1>Login Success! 🎉</h1>" +
                "<h2>Hello, " + name + "</h2>" +
                "<p>Email: " + email + "</p>" +
                "<p>This response is from <b>Client Backend</b>.</p>";
    }

    @GetMapping("/token")
    public String getToken(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client) {
        // Ikkada magic undi. Spring already token techukundi, daanni manam adugutunnam
        String tokenValue = client.getAccessToken().getTokenValue();

        return "<h1>Secret Access Token 🤫</h1>" +
                "<p>Idi Google manaki ichina Pass:</p>" +
                "<div style='word-wrap:break-word; background:#f4f4f4; padding:10px; border:1px solid #ddd;'>" +
                tokenValue +
                "</div>";
    }

    // Example : Principal object (key: value pair)
    // Name: "My TV"
    // Email: "mytv35049@gmail.com"
    // Attributes: {
    // "sub": "100897...",
    // "name": "My TV",
    // "given_name": "My",
    // "picture": "https://lh3.google...",
    // "email": "mytv35049@gmail.com",
    // "email_verified": true
    // }
    @GetMapping("/user")
    public String home(
            // ----- USER DETAILS
            // 1. Ikkada manam User Details adugutunnam (Like Name, Email)
            // Spring Mawa, SecurityContext (Session) lo evadaite user unnado, vaadi details
            // ni direct ga techi principal ane variable lo pettu
            // Google manaki return lo User Data ni JSON format lo istundi (name, email,
            // picture, sub).
            // Spring Security aa JSON ni OAuth2User ane Java Object ga marchuthundi.
            @AuthenticationPrincipal OAuth2User principal,

            // ---- TOKEN DETAILS
            // 2. ⚠️ Ikkada manam "Vault" nundi Token Packet adugutunnam
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client) {

        // Idi chala patha paddathi (Don't use this) - Alternative of
        // '@AuthenticationPrincipal OAuth2User principal'
        // Authentication authentication =
        // SecurityContextHolder.getContext().getAuthentication();
        // OAuth2User user = (OAuth2User) authentication.getPrincipal();
        // String email = user.getAttribute("email");hashIsZero = false

        // Let's extract the Token String
        String tokenValue = client.getAccessToken().getTokenValue();

        // Log it to see in Console (Or put a debug point here)
        System.out.println("---------------------------------------------");
        System.out.println("🔥 MAWA! I FOUND THE TOKEN: " + tokenValue);
        System.out.println("---------------------------------------------");

        return "<h1>Welcome Mawa!</h1>" +
                "<h3>User: " + principal.getAttribute("name") + "</h3>" +
                "<h3>Email: " + principal.getAttribute("email") + "</h3>" +
                "<p>Check your backend console logs for the Access Token!</p>";
    }

    // ========== 🔥 DEBUG ENDPOINT - Token Deep Inspection ==========
    @GetMapping("/debug/tokens")
    public String debugTokens(
            @AuthenticationPrincipal OidcUser oidcUser,
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><style>");
        sb.append("body { font-family: Arial; padding: 20px; background: #1a1a2e; color: #eee; }");
        sb.append(
                ".token-box { background: #16213e; padding: 15px; border-radius: 8px; margin: 10px 0; word-wrap: break-word; }");
        sb.append(".label { color: #e94560; font-weight: bold; }");
        sb.append("h1 { color: #0f4c75; }");
        sb.append("</style></head><body>");

        sb.append("<h1>🔍 TOKEN DEBUG DASHBOARD</h1>");

        // ===== 1. ACCESS TOKEN =====
        String accessToken = client.getAccessToken().getTokenValue();
        System.out.println("\n============ 🔑 ACCESS TOKEN ============");
        System.out.println(accessToken);
        System.out.println("Token Type: " + client.getAccessToken().getTokenType().getValue());
        System.out.println("Expires At: " + client.getAccessToken().getExpiresAt());
        System.out.println("Scopes: " + client.getAccessToken().getScopes());

        sb.append("<h2>1️⃣ Access Token (Opaque - NOT JWT)</h2>");
        sb.append("<div class='token-box'>");
        sb.append("<p class='label'>Value:</p>");
        sb.append("<code>" + accessToken + "</code>");
        sb.append("<p class='label'>Type:</p><p>" + client.getAccessToken().getTokenType().getValue() + "</p>");
        sb.append("<p class='label'>Expires:</p><p>" + client.getAccessToken().getExpiresAt() + "</p>");
        sb.append("<p class='label'>Scopes:</p><p>" + client.getAccessToken().getScopes() + "</p>");
        sb.append("</div>");

        // ===== 2. ID TOKEN (THIS IS THE JWT!) =====
        if (oidcUser != null && oidcUser.getIdToken() != null) {
            String idToken = oidcUser.getIdToken().getTokenValue();
            System.out.println("\n============ 🎫 ID TOKEN (JWT!) ============");
            System.out.println(idToken);
            System.out.println("Subject: " + oidcUser.getSubject());
            System.out.println("Issuer: " + oidcUser.getIdToken().getIssuer());
            System.out.println("Audience: " + oidcUser.getIdToken().getAudience());
            System.out.println("Issued At: " + oidcUser.getIdToken().getIssuedAt());
            System.out.println("Expires At: " + oidcUser.getIdToken().getExpiresAt());

            sb.append("<h2>2️⃣ ID Token (JWT - THIS IS WHAT WE NEED!) ✅</h2>");
            sb.append("<div class='token-box' style='border: 2px solid #00ff00;'>");
            sb.append("<p class='label'>JWT Value:</p>");
            sb.append("<code style='color: #00ff00;'>" + idToken + "</code>");
            sb.append("<p class='label'>Subject (sub):</p><p>" + oidcUser.getSubject() + "</p>");
            sb.append("<p class='label'>Issuer:</p><p>" + oidcUser.getIdToken().getIssuer() + "</p>");
            sb.append("<p class='label'>Audience:</p><p>" + oidcUser.getIdToken().getAudience() + "</p>");
            sb.append("<p class='label'>Claims:</p><p>" + oidcUser.getClaims() + "</p>");
            sb.append("</div>");

            // Check if it's a real JWT (has 3 parts)
            String[] jwtParts = idToken.split("\\.");
            if (jwtParts.length == 3) {
                sb.append(
                        "<h3 style='color: #00ff00;'>✅ ID Token is a valid JWT format (3 parts: header.payload.signature)</h3>");
                System.out.println("\n✅ ID TOKEN IS A VALID JWT! We can use this for Resource Server!");
            }
        } else {
            sb.append("<h2 style='color: red;'>❌ No ID Token found!</h2>");
            System.out.println("\n❌ NO ID TOKEN FOUND!");
        }

        // ===== 3. REFRESH TOKEN =====
        if (client.getRefreshToken() != null) {
            System.out.println("\n============ 🔄 REFRESH TOKEN ============");
            System.out.println(client.getRefreshToken().getTokenValue());

            sb.append("<h2>3️⃣ Refresh Token</h2>");
            sb.append("<div class='token-box'>");
            sb.append("<code>" + client.getRefreshToken().getTokenValue() + "</code>");
            sb.append("</div>");
        } else {
            sb.append("<h2>3️⃣ Refresh Token: Not Available</h2>");
            System.out.println("\n❌ NO REFRESH TOKEN (Need 'access_type=offline' scope)");
        }

        sb.append("<hr><h2>📋 Next Steps</h2>");
        sb.append("<p>1. Copy the <b>ID Token (JWT)</b> from above</p>");
        sb.append(
                "<p>2. Send it to Resource Server: <code>curl -H 'Authorization: Bearer {ID_TOKEN}' http://localhost:8081/photos</code></p>");

        sb.append("</body></html>");

        return sb.toString();
    }

    // ========== 🚀 API ENDPOINT - Proxy to Resource Server ==========
    // Idi manam React nundi call chestam
    // Backend Resource Server ni call chesi response return chestundi
    @GetMapping("/api/photos")
    public String getPhotosFromResourceServer(
            @AuthenticationPrincipal OidcUser oidcUser,
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client) {
        // 1. Get ID Token (JWT) - NOT Access Token!
        if (oidcUser == null || oidcUser.getIdToken() == null) {
            return "{\"error\": \"No ID Token found. Please login again.\"}";
        }

        String idToken = oidcUser.getIdToken().getTokenValue();
        System.out.println("\n🚀 CALLING RESOURCE SERVER with ID Token (JWT)...");
        System.out.println("Token (first 50 chars): " + idToken.substring(0, Math.min(50, idToken.length())) + "...");

        // 2. Call Resource Server with Bearer Token
        try {
            RestTemplate restTemplate = new RestTemplate();

            // Create headers with Authorization
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setBearerAuth(idToken);

            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

            // Call Resource Server
            org.springframework.http.ResponseEntity<String> response = restTemplate.exchange(
                    "http://localhost:8081/photos",
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    String.class);

            System.out.println("✅ RESOURCE SERVER RESPONSE: " + response.getStatusCode());
            return response.getBody();
        } catch (Exception e) {
            System.out.println("❌ ERROR calling Resource Server: " + e.getMessage());
            e.printStackTrace();
            return "<h1 style='color: red;'>❌ Error calling Resource Server</h1>" +
                    "<p><b>Error:</b> " + e.getMessage() + "</p>" +
                    "<p><b>Hint:</b> Make sure Resource Server is running on port 8081</p>" +
                    "<p><a href='/debug/tokens'>View Token Debug</a></p>";
        }
    }

    // ========== 🔥 SESSION DEBUG - SEE WHAT'S STORED! ==========
    @GetMapping("/debug/session")
    public String debugSession(
            jakarta.servlet.http.HttpServletRequest request,
            @AuthenticationPrincipal OidcUser oidcUser,
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient client) {

        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><style>");
        sb.append("body { font-family: Arial; background: #0d1117; color: #c9d1d9; padding: 20px; }");
        sb.append(
                ".box { background: #161b22; border: 1px solid #30363d; border-radius: 8px; padding: 20px; margin: 15px 0; }");
        sb.append(".title { color: #58a6ff; font-size: 1.3em; }");
        sb.append(".key { color: #7ee787; }");
        sb.append(".value { color: #f0883e; word-break: break-all; }");
        sb.append("pre { background: #21262d; padding: 10px; border-radius: 5px; }");
        sb.append("</style></head><body>");

        sb.append("<h1>🔍 SESSION DEEP INSPECTION</h1>");

        // SESSION INFO
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        sb.append("<div class='box'><div class='title'>📦 SESSION INFO</div>");
        if (session != null) {
            sb.append("<p><span class='key'>Session ID:</span> <span class='value'>" + session.getId() + "</span></p>");
            sb.append("<p><span class='key'>Created:</span> " + new java.util.Date(session.getCreationTime()) + "</p>");
        }
        sb.append("</div>");

        // USER DATA FROM GOOGLE
        sb.append("<div class='box'><div class='title'>👤 GOOGLE NUNDI VACHINA USER DATA</div>");
        if (oidcUser != null) {
            sb.append("<p><span class='key'>Subject (User ID):</span> <span class='value'>" + oidcUser.getSubject()
                    + "</span></p>");
            sb.append(
                    "<p><span class='key'>Name:</span> <span class='value'>" + oidcUser.getFullName() + "</span></p>");
            sb.append("<p><span class='key'>Email:</span> <span class='value'>" + oidcUser.getEmail() + "</span></p>");
            sb.append("<p><span class='key'>Picture:</span> <span class='value'>" + oidcUser.getPicture()
                    + "</span></p>");
            sb.append(
                    "<p><span class='key'>Issuer:</span> <span class='value'>" + oidcUser.getIssuer() + "</span></p>");
            sb.append("<h4>All Claims:</h4><pre>");
            oidcUser.getClaims().forEach((key, value) -> sb.append(key + ": " + value + "\n"));
            sb.append("</pre>");
        }
        sb.append("</div>");

        // TOKENS
        sb.append("<div class='box'><div class='title'>🎫 TOKENS STORED IN SESSION</div>");
        if (client != null) {
            String accessToken = client.getAccessToken().getTokenValue();
            sb.append("<h4>1️⃣ Access Token (Opaque):</h4>");
            sb.append("<pre>" + accessToken.substring(0, Math.min(80, accessToken.length())) + "...</pre>");

            if (oidcUser != null && oidcUser.getIdToken() != null) {
                String idToken = oidcUser.getIdToken().getTokenValue();
                sb.append("<h4>2️⃣ ID Token (JWT):</h4>");
                sb.append("<pre>" + idToken.substring(0, Math.min(80, idToken.length())) + "...</pre>");
            }
        }
        sb.append("</div>");

        // SUMMARY
        sb.append("<div class='box'><div class='title'>📋 SUMMARY</div>");
        sb.append("<pre>SESSION {\n");
        sb.append("  id: \"" + (session != null ? session.getId().substring(0, 8) + "...\"" : "null") + ",\n");
        sb.append("  user: { name: \"" + (oidcUser != null ? oidcUser.getFullName() : "") + "\", email: \""
                + (oidcUser != null ? oidcUser.getEmail() : "") + "\" },\n");
        sb.append("  tokens: { accessToken: \"ya29...\", idToken: \"eyJhbG...\" }\n");
        sb.append("}</pre></div>");

        sb.append("</body></html>");
        return sb.toString();
    }
}
