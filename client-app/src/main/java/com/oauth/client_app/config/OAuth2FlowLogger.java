package com.oauth.client_app.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 🎓 OAUTH2 FLOW LOGGER
 * 
 * Mawa! Ee class prathi HTTP request ni intercept chesi,
 * step-by-step em jarugutundo print chestundi.
 * 
 * Idi FIRST filter - anni requests ikkada first vastaayi!
 */
@Component
@Order(Integer.MIN_VALUE) // Run this FIRST before any other filter
public class OAuth2FlowLogger implements Filter {

    private static final String BORDER = "\n" + "═".repeat(80);
    private static final String LINE = "─".repeat(80);

    private int requestCounter = 0;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        String queryString = httpRequest.getQueryString();

        // Skip static resources
        if (uri.contains(".") && !uri.contains("/oauth2/") && !uri.contains("/login/")) {
            chain.doFilter(request, response);
            return;
        }

        requestCounter++;

        System.out.println(BORDER);
        System.out.println("🚀 REQUEST #" + requestCounter + " - " + method + " " + uri);
        System.out.println(BORDER);

        // ═══════════════════════════════════════════════════════════════════
        // STEP 1: REQUEST DETAILS
        // ═══════════════════════════════════════════════════════════════════
        System.out.println("\n📥 STEP 1: REQUEST VACHINDI (Incoming Request)");
        System.out.println(LINE);
        System.out.println("   🌐 URI: " + uri);
        System.out.println("   📝 Method: " + method);
        if (queryString != null) {
            System.out.println("   📎 Query Params: " + queryString);

            // Check for OAuth callback params
            if (queryString.contains("code=")) {
                System.out.println("\n   🎫 AUTHORIZATION CODE FOUND!");
                System.out.println("   💡 Idi Google ichina 'Temporary Pass' - idi tokens ki exchange avtundi");
            }
            if (queryString.contains("state=")) {
                System.out.println("   🔐 STATE PARAMETER FOUND!");
                System.out
                        .println("   💡 Idi CSRF protection kosam - manam pampina state match avtundo check chestaru");
            }
        }

        // ═══════════════════════════════════════════════════════════════════
        // STEP 2: SESSION DETAILS
        // ═══════════════════════════════════════════════════════════════════
        System.out.println("\n📦 STEP 2: SESSION CHECK (Server-side storage)");
        System.out.println(LINE);
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            System.out.println("   ✅ Session EXISTS");
            System.out.println("   🆔 Session ID: " + session.getId().substring(0, 8) + "...");
            System.out.println("   ⏰ Created: " + new java.util.Date(session.getCreationTime()));
        } else {
            System.out.println("   ❌ No Session Yet - First time visitor or logged out");
            System.out.println("   💡 Login tarvata session create avtundi");
        }

        // ═══════════════════════════════════════════════════════════════════
        // STEP 3: AUTHENTICATION STATUS
        // ═══════════════════════════════════════════════════════════════════
        System.out.println("\n🔑 STEP 3: AUTHENTICATION STATUS (Logged in aa kaadaa?)");
        System.out.println(LINE);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof OidcUser) {
            OidcUser user = (OidcUser) auth.getPrincipal();
            System.out.println("   ✅ User AUTHENTICATED!");
            System.out.println("   👤 Name: " + user.getFullName());
            System.out.println("   📧 Email: " + user.getEmail());
            System.out.println("   🎫 ID Token exists: " + (user.getIdToken() != null));
        } else {
            System.out.println("   ❌ User NOT authenticated yet");
            System.out.println("   💡 Ee request login require chestundi or public endpoint ki vellali");
        }

        // ═══════════════════════════════════════════════════════════════════
        // STEP 4: WHICH FILTER WILL HANDLE THIS?
        // ═══════════════════════════════════════════════════════════════════
        System.out.println("\n⚙️ STEP 4: EE REQUEST NI EVARU HANDLE CHESTARU?");
        System.out.println(LINE);

        if (uri.equals("/oauth2/authorization/google")) {
            System.out.println("   🎯 TARGET: OAuth2AuthorizationRequestRedirectFilter");
            System.out.println("   📍 LOCATION: Spring Security internal filter");
            System.out.println("   ");
            System.out.println("   🔥 EM JARUGUTUNDI:");
            System.out.println("      1️⃣ State parameter generate chestundi (CSRF protection)");
            System.out.println("      2️⃣ State ni session lo save chestundi");
            System.out.println("      3️⃣ Google Authorization URL build chestundi");
            System.out.println("      4️⃣ 302 Redirect response istundi → Google ki vellipothav");
            System.out.println("   ");
            System.out.println("   📤 REDIRECT TO: https://accounts.google.com/o/oauth2/v2/auth");

        } else if (uri.equals("/login/oauth2/code/google")) {
            System.out.println("   🎯 TARGET: OAuth2LoginAuthenticationFilter");
            System.out.println("   📍 LOCATION: Spring Security internal filter");
            System.out.println("   ");
            System.out.println("   🔥 EM JARUGUTUNDI:");
            System.out.println("      1️⃣ URL lo state parameter extract chestundi");
            System.out.println("      2️⃣ Session lo saved state tho compare chestundi (CSRF check)");
            System.out.println("      3️⃣ Authorization code extract chestundi");
            System.out.println("      4️⃣ Google Token Endpoint ki POST request chestundi:");
            System.out.println("         - URL: https://oauth2.googleapis.com/token");
            System.out.println("         - Body: client_id, client_secret, code, redirect_uri");
            System.out.println("      5️⃣ Google nundi tokens receive chestundi:");
            System.out.println("         - Access Token (opaque)");
            System.out.println("         - ID Token (JWT!)");
            System.out.println("      6️⃣ Tokens ni session lo save chestundi");
            System.out.println("      7️⃣ OidcUser object create chestundi");
            System.out.println("      8️⃣ 302 Redirect istundi → React ki vellipothav");
            System.out.println("   ");
            System.out.println("   📤 REDIRECT TO: http://localhost:5173/dashboard");

        } else if (uri.startsWith("/api/")) {
            System.out.println("   🎯 TARGET: Your Controller (ClientBackendController)");
            System.out.println("   📍 LOCATION: com.oauth.client_app.controller");
            System.out.println("   ");
            System.out.println("   🔥 EM JARUGUTUNDI:");
            System.out.println("      1️⃣ First ga AnonymousAuthenticationFilter check chestundi");
            System.out.println("      2️⃣ If authenticated - Controller ki pass avtundi");
            System.out.println("      3️⃣ Controller nundi @AuthenticationPrincipal use chesi user info access");

            if (uri.equals("/api/photos")) {
                System.out.println("   ");
                System.out.println("   📸 /api/photos - SPECIAL FLOW:");
                System.out.println("      1️⃣ Session nundi ID Token (JWT) extract chestaru");
                System.out.println("      2️⃣ Resource Server (8081) ki call chestaru with JWT");
                System.out.println("      3️⃣ Header: Authorization: Bearer <JWT>");
            }

        } else if (uri.equals("/logout")) {
            System.out.println("   🎯 TARGET: LogoutFilter");
            System.out.println("   📍 LOCATION: Spring Security internal filter");
            System.out.println("   ");
            System.out.println("   🔥 EM JARUGUTUNDI:");
            System.out.println("      1️⃣ Session invalidate chestundi");
            System.out.println("      2️⃣ JSESSIONID cookie delete chestundi");
            System.out.println("      3️⃣ SecurityContext clear chestundi");
            System.out.println("      4️⃣ Redirect to React home page");

        } else {
            System.out.println("   🎯 TARGET: Default handler or Controller");
            System.out.println("   💡 Normal request - authentication check tarvata process avtundi");
        }

        System.out.println("\n" + LINE);
        System.out.println("▶️ NOW PASSING TO SPRING SECURITY FILTER CHAIN...");
        System.out.println(LINE + "\n");

        // Let the request pass through the filter chain
        chain.doFilter(request, response);

        // ═══════════════════════════════════════════════════════════════════
        // STEP 5: RESPONSE ANALYSIS (After all filters processed)
        // ═══════════════════════════════════════════════════════════════════
        int status = httpResponse.getStatus();

        System.out.println("\n" + LINE);
        System.out.println("📤 STEP 5: RESPONSE READY (Filter chain complete)");
        System.out.println(LINE);
        System.out.println("   📊 Status Code: " + status);

        if (status == 302) {
            String location = httpResponse.getHeader("Location");
            System.out.println("   🔄 REDIRECT HAPPENING!");
            System.out.println("   📍 Location: " + location);

            if (location != null && location.contains("accounts.google.com")) {
                System.out.println("   💡 Google Login page ki redirect avtunnaav!");
                System.out.println("   ");
                System.out.println("   🔥 NEXT STEPS:");
                System.out.println("      1️⃣ Browser Google login page show chestundi");
                System.out.println("      2️⃣ Nuvvu credentials enter chesi login avtaav");
                System.out.println("      3️⃣ Google /login/oauth2/code/google ki redirect chestundi");
                System.out.println("      4️⃣ Aa request manam handle chesi tokens techukunatam");
            } else if (location != null && location.contains("localhost:5173")) {
                System.out.println("   💡 React UI ki redirect avtunnaav!");
                System.out.println("   🎉 LOGIN SUCCESSFUL - React app lo dashboard chustav!");
            }
        } else if (status == 200) {
            System.out.println("   ✅ SUCCESS - Data returned!");
        } else if (status == 401) {
            System.out.println("   ❌ UNAUTHORIZED - Login required!");
        } else if (status == 403) {
            System.out.println("   ⛔ FORBIDDEN - Permission denied!");
        }

        // Check post-request authentication
        auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof OidcUser) {
            System.out.println("   🔓 User is now AUTHENTICATED after this request!");
        }

        System.out.println(BORDER);
        System.out.println("✅ REQUEST #" + requestCounter + " COMPLETE\n");
    }
}
