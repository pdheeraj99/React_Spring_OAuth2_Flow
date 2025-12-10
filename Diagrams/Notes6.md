Nuvvu backend project motham search chesina `/oauth2/authorization/google` ane Endpoint (`@GetMapping`) neeku dorakadu. **Asalu Undadu.**

Mari work ela avtundi?
Answer simple: **Idi Controller daka velladu. Gate daggare Pani aipothundi.**

Deenni **"Spring Security Filters Magic"** antaru. Let me explain clearly.

-----

### 🛑 Controller vs Filter (The Difference)

Imagine nee Backend Application oka **Apartment** anuko.

1.  **Filters:** Apartment Gate daggara unde **Security Guards**. Prathi request veella deggara aagalsinde.
2.  **Controllers:** Apartment lopala unde **Flats**.

**Normal Request (Eg: `/home`):**

  * Request Gate (Filter) datukoni -\> Lopala unna Flat (Controller) ki veltundi.

**Google Login Request (`/oauth2/authorization/google`):**

  * Request Gate daggara ki vastundi.
  * Akkada **Specific Guard** (Filter) untadu. Vaadi peru: `OAuth2AuthorizationRequestRedirectFilter`.
  * Vadu URL chustadu: *"Oh, veedu Google login adugutunnadu."*
  * **Action:** Vadu lopala unna Flat (Controller) ki call cheyadu. Gate daggare Google URL construct chesi, *"Babu, nuvvu Google ki vellu"* ani `302 Redirect` ichi pampichestadu.
  * **Result:** Request Controller daka asalu cheradu. Anduke neeku Code kanipinchadu.

-----

### 🛠️ The Technical Proof

Spring Security jar lo `OAuth2AuthorizationRequestRedirectFilter` ane class default ga oka URL pattern kosam wait chestu untundi:

**Pattern:** `/oauth2/authorization/{registrationId}`

  * Manam YAML file lo `registration: google` ani pettam kada?
  * So, automatic ga magic URL: `/oauth2/authorization/google` create aipothundi.

### 🔍 Code lo ela untundi (Internal Library Code)?

Manam rayale, kani Library lo logic ila untundi (Simplified):

```java
// Idi mana code kadu, Spring Security internal code
public class OAuth2AuthorizationRequestRedirectFilter extends OncePerRequestFilter {

    // Default URL matcher
    private RequestMatcher requestMatcher = new AntPathRequestMatcher("/oauth2/authorization/{registrationId}");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        
        // 1. Check if URL matches "/oauth2/authorization/google"
        if (this.requestMatcher.matches(request)) {
            
            // 2. Construct Google URL (using YAML config)
            String googleUrl = buildOAuthUrl(...);
            
            // 3. Send Redirect (302) IMMEDIATELY
            response.sendRedirect(googleUrl);
            
            // 🛑 STOP! Don't go to Controller (return)
            return; 
        }

        // Match avvakapothe next filter/controller ki vellu
        filterChain.doFilter(request, response);
    }
}
```

### 💡 40LPA Logic

Nuvvu okavela ee URL naku nachaledu, nenu `/login/google` ani pettukunta ante marchukovachu.
Kani Standard practice entante Spring default URL `/oauth2/authorization/...` ne vadatam. Endukante team lo evaru join aina code chudakunda URL guess cheyagalugutharu.