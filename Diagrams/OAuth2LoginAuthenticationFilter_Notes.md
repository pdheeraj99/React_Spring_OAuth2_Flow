Nuvvu upload chesina Image (URL with `code=...`) ni, ee Java Code ela handle chestundo **Line-by-Line Operation** cheddam.

Idi `OAuth2LoginAuthenticationFilter` class. Idi **Step 4 & 5** (Code receive cheskuni, Token techukune process) ki **Manager** laanti di.

-----

### 🕵️‍♂️ Code Walkthrough (Line-by-Line Logic)

Manam Google nundi return vacham. URL lo `code` undi.
Browser hits: `http://localhost:8080/login/oauth2/code/google?code=XYZ...&state=ABC...`

#### 1\. The Trap (Filter Trigger) 🪤

```java
public static final String DEFAULT_FILTER_PROCESSES_URI = "/login/oauth2/code/*";
```

  * **Logic:** Ee Filter kurchuni chustu untundi.
  * **Action:** Epudaithe URL `/login/oauth2/code/...` pattern lo vastundo, **Spring Security** ee Class ni nidra leputundi. "Le, Google nundi response vachindi\!"

#### 2\. Catching the Params (Code & State) 🎣

```java
MultiValueMap<String, String> params = OAuth2AuthorizationResponseUtils.toMultiMap(request.getParameterMap());
if (!OAuth2AuthorizationResponseUtils.isAuthorizationResponse(params)) { ... error ... }
```

  * **Logic:** URL lo unna `?code=...` and `&state=...` ni grab chestundi.
  * **Validation:** Asalu `code` parameter unda leda? Lekapothe Error throw chestundi.

#### 3\. The Memory Check (Security Validation) 🧠

```java
OAuth2AuthorizationRequest authorizationRequest = this.authorizationRequestRepository
      .removeAuthorizationRequest(request, response);
```

  * **40LPA Concept:** Nuvvu Google ki velle mundu, Spring Security nee Request details ni **Session** lo dachipettindi (Remember `state` param?).
  * **Check:** Ippudu return vachaka, aa patha request ni bayataki tisi, "Idi manam pampinchina `state` ye na? Leka Hacker pampinchina fake response ah?" ani check chestundi. Match avvakapothe **Reject**.

#### 4\. Finding the Config (Who is this?) 📂

```java
String registrationId = authorizationRequest.getAttribute(OAuth2ParameterNames.REGISTRATION_ID);
ClientRegistration clientRegistration = this.clientRegistrationRepository.findByRegistrationId(registrationId);
```

  * **Logic:** URL lo unna `google` (registrationId) ni tiskoni, nee `application.yml` lo unna `client-id`, `client-secret` details ni load chestundi.

#### 5\. The Main Event (Exchange Code for Token) 💥

**⚠️ Idi Most Important Line:**

```java
OAuth2LoginAuthenticationToken authenticationResult = (OAuth2LoginAuthenticationToken) this
      .getAuthenticationManager()
      .authenticate(authenticationRequest);
```

  * **Explanation:**
      * Ee filter direct ga Google ki call cheyadu (Idi Manager).
      * Idi **`AuthenticationManager`** ki command istundi: *"Naa daggara Code undi, Secret undi. Velli Google tho matladi Token te."*
      * Aa `AuthenticationManager` lopala **`OAuth2LoginAuthenticationProvider`** (Worker) ni pilichi, **HTTP Request** (`RestTemplate`) pampinchi, **Access Token** ni techukuntundi.
      * **Result:** `authenticationResult` lo ippudu **Access Token** vachesindi\!

#### 6\. Saving to Vault (Authorized Client Repo) 🏦

```java
OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(... authenticationResult.getAccessToken(), ...);
this.authorizedClientRepository.saveAuthorizedClient(authorizedClient, ...);
```

  * **Logic:** Vachina **Access Token** ni `AuthorizedClientRepository` lo save chestundi (Usually in Memory/Session).
  * **Why:** Repu manam **Resource Server** ni call cheyali ante Token ikkadi nunde tiskuntam.

#### 7\. Final Success (Return to Controller) ✅

```java
return oauth2Authentication;
```

  * **Action:** Authentication Success\!
  * Next steps lo Spring Security session create chesi, browser ni Home Page (`/`) ki redirect chestundi.

-----

### 🖼️ The Visual Flow (Behind the Code)

Nuvvu chustunna Code flow diagram lo ila untundi:

### 🎯 Summary (Mawa Language lo)

1.  **Trap:** URL `/login/oauth2/code/google` kanipinchagane ee Filter active aindi.
2.  **Grab:** URL lo unna `code` ni pattukundi.
3.  **Verify:** "Idi manam adigina code ye na?" ani patha session tho check chesindi.
4.  **Delegate:** "Orey Manager, ee code tiskelli Google ki ichi, Token patura\!" ani order vesindi.
5.  **Store:** Vachina Token ni safe ga dachipettindi.
6.  **Success:** "Mawa Login aipoyadu, Home page ki pampinchu" ani cheppindi.

Super clear kada? Ee code block ye **Heart of the Client**.

Ippudu mana daggara **Token** undi (Step 6 lo save chesam).
Next **Resource Server (Project 2)** create cheddam. Appudu ee Token ni vaadi akkada unna "Secret Photos" ni ela access cheyalo chuddam. 