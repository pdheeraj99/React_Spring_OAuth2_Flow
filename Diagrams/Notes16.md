Perfect! 💯 Ippudu exact code tho cheptanu! 

## **🔥 COMPLETE FLOW - LINE BY LINE 🔥**

### **Step 1️⃣: OAuth2LoginAuthenticationFilter (Line 166)**

```java
// OAuth2LoginAuthenticationFilter.java
// Line 159-166 - Meeru ichina code

OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(
    authenticationResult.getClientRegistration(), 
    oauth2Authentication.getName(),
    authenticationResult.getAccessToken(),    // Access token ikkada
    authenticationResult.getRefreshToken());  // Refresh token ikkada

this.authorizedClientRepository.saveAuthorizedClient(
    authorizedClient, oauth2Authentication, request, response);
    // 👆 Line 166: Ikkada call avtundi!
```

### **Step 2️⃣: HttpSessionOAuth2AuthorizedClientRepository (Line 57-63)**

```java
// HttpSessionOAuth2AuthorizedClientRepository.java
// Line 57-63 - SESSION STORAGE METHOD

@Override
public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, 
      Authentication principal,
      HttpServletRequest request, 
      HttpServletResponse response) {
   
   Assert.notNull(authorizedClient, "authorizedClient cannot be null");
   Assert.notNull(request, "request cannot be null");
   Assert.notNull(response, "response cannot be null");
   
   // Line 61: Existing clients teesukuntundi (leda new map create chestundi)
   Map<String, OAuth2AuthorizedClient> authorizedClients = 
       this.getAuthorizedClients(request);  // 👈 Method call
   
   // Line 62: Google ki map lo add chestundi with token!
   authorizedClients.put(
       authorizedClient.getClientRegistration().getRegistrationId(),  // "google"
       authorizedClient);  // Access token + Refresh token + user info
   
   // Line 63: 🔥 SESSION LO STORE AVTUNDI! 🔥
   request.getSession().setAttribute(
       this.sessionAttributeName,    // "AUTHORIZED_CLIENTS"
       authorizedClients);           // Map with tokens
}
```

### **Step 3️⃣: getAuthorizedClients() Method (Line 81-90)**

```java
// HttpSessionOAuth2AuthorizedClientRepository.java
// Line 81-90 - GET/CREATE SESSION

@SuppressWarnings("unchecked")
private Map<String, OAuth2AuthorizedClient> getAuthorizedClients(
        HttpServletRequest request) {
   
   // Line 82: 🔥 SESSION ACCESS/CREATE JARIGEDI IKKADA! 🔥
   HttpSession session = request.getSession(false);
   // 👆 false = "Already unte ivvu, lekupothe null ivvu (don't create)"
   
   // Line 83-84: Session nundi existing clients teesukuntundi
   Map<String, OAuth2AuthorizedClient> authorizedClients = (session != null)
       ? (Map<String, OAuth2AuthorizedClient>) session.getAttribute(
             this.sessionAttributeName) 
       : null;
   
   // Line 85-87: First time aithe new HashMap create chestundi
   if (authorizedClients == null) {
       authorizedClients = new HashMap<>();
   }
   
   return authorizedClients;
}
```

### **⚠️ CRITICAL DISCOVERY!**

**Line 82** lo `request.getSession(false)` undi - idi session create cheyyadu!

**BUT**, **Line 63** lo `request.getSession()` parameter ledu ante **default `true`** avtundi!

```java
// Line 63 actual behavior:
request.getSession()  // Same as request.getSession(true)
// 👆 Creates session if it doesn't exist!
```

## **🎯 EXACT SESSION CREATION POINT**

```java
// HttpSessionOAuth2AuthorizedClientRepository.java - Line 63

request.getSession().setAttribute(this.sessionAttributeName, authorizedClients);
         👆
         |
    getSession() without parameter = getSession(true)
         |
         ↓
   Creates session if not exists!
   Generates JSESSIONID: "ABC123XYZ"
   Creates cookie and adds to response
```

## **📊 COMPLETE VISUALIZATION - WITH LINE NUMBERS**

```
┌─────────────────────────────────────────────────────────────────┐
│ OAuth2LoginAuthenticationFilter.attemptAuthentication()        │
│ Line 166: authorizedClientRepository.saveAuthorizedClient()    │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ HttpSessionOAuth2AuthorizedClientRepository                    │
│ Line 57-63: saveAuthorizedClient() method                      │
│                                                                 │
│ Line 61: Map<> authorizedClients = getAuthorizedClients();     │
│ Line 62: authorizedClients.put("google", authorizedClient);    │
│ Line 63: request.getSession().setAttribute(...);  🔥 KEY LINE! │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ request.getSession() Internal Implementation                   │
│ (Servlet Container - Tomcat)                                    │
│                                                                 │
│ 1. Check if session exists                                     │
│ 2. If NOT exists → Create new HttpSession                      │
│ 3. Generate Session ID: "ABC123XYZ"                            │
│ 4. Store in server memory                                       │
│ 5. Create Cookie: JSESSIONID=ABC123XYZ                         │
│ 6. Add cookie to HttpServletResponse                           │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ Session Storage (Server Memory)                                │
│                                                                 │
│ HttpSession [ID: ABC123XYZ]                                     │
│   ├─ Attribute: "...AUTHORIZED_CLIENTS"                        │
│   │   └─ Map<String, OAuth2AuthorizedClient>                   │
│   │       └─ Key: "google"                                     │
│   │           └─ OAuth2AuthorizedClient:                       │
│   │               ├─ accessToken: "ya29.a0AfB..."              │
│   │               ├─ refreshToken: "1//0gL3..."                │
│   │               ├─ principal: "yourname@gmail.com"           │
│   │               └─ expiresAt: timestamp                      │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ HTTP Response to Browser                                        │
│                                                                 │
│ HTTP/1.1 302 Found                                              │
│ Set-Cookie: JSESSIONID=ABC123XYZ; HttpOnly; Path=/             │
│ Location: http://localhost:3000/dashboard                      │
└─────────────────────────────────────────────────────────────────┘
```

## **🔑 KEY LINES SUMMARY**

| File | Line | Code | Purpose |
|------|------|------|---------|
| `OAuth2LoginAuthenticationFilter` | 166 | `authorizedClientRepository.saveAuthorizedClient()` | Token storage trigger chestundi |
| `HttpSessionOAuth2AuthorizedClientRepository` | 61 | `getAuthorizedClients(request)` | Existing session data teesukuntundi |
| `HttpSessionOAuth2AuthorizedClientRepository` | 62 | `authorizedClients.put("google", ...)` | Token map lo add chestundi |
| `HttpSessionOAuth2AuthorizedClientRepository` | **63** | `request.getSession().setAttribute(...)` | **🔥 SESSION CREATE + STORAGE** |
| `HttpSessionOAuth2AuthorizedClientRepository` | 82 | `request.getSession(false)` | Existing session check chestundi (create cheyyadu) |

## **💡 ADD TO YOUR DIAGRAM:**

Mee diagram lo "gets access token" taruvata idi add cheyandi:

```
gets access token
     │
     ↓
OAuth2LoginAuthenticationFilter
Line 166: authorizedClientRepository.saveAuthorizedClient()
     │
     ↓
HttpSessionOAuth2AuthorizedClientRepository  
Line 61: getAuthorizedClients(request)
     │
     ↓
Line 63: request.getSession().setAttribute() 🔥
     │
     ├─→ Session Creation (if not exists)
     │   └─ JSESSIONID = "ABC123XYZ"
     │
     └─→ Token Storage
         ├─ Access Token stored ✓
         ├─ Refresh Token stored ✓
         └─ User Principal stored ✓
     │
     ↓
Set-Cookie: JSESSIONID=ABC123XYZ
Redirect to http://localhost:3000
```

Ippudu 100% clear ayyinda? Exact line numbers tho! 🎯🚀