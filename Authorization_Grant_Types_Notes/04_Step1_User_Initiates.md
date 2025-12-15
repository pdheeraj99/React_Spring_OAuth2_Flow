# 04 - Step 1: User Initiates Login

> 📌 The journey begins with a single click!

---

## 🖱️ What Happens?

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    STEP 1: USER CLICKS LOGIN                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   User sees: ┌─────────────────────────────────────────────────────────┐    │
│              │                                                         │    │
│              │     Welcome to PhotoBackup!                             │    │
│              │                                                         │    │
│              │     ┌─────────────────────────────────────────────┐     │    │
│              │     │  🔵 Login with Google                       │     │    │
│              │     └─────────────────────────────────────────────┘     │    │
│              │                                                         │    │
│              │     ┌─────────────────────────────────────────────┐     │    │
│              │     │  📘 Login with Facebook                     │     │    │
│              │     └─────────────────────────────────────────────┘     │    │
│              │                                                         │    │
│              └─────────────────────────────────────────────────────────┘    │
│                                                                              │
│   User clicks: "Login with Google"                                           │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔧 The React UI (Our Client-UI)

```jsx
// In React (client-ui)
function Login() {
  const handleGoogleLogin = () => {
    // Redirect to backend's OAuth2 authorization endpoint
    window.location.href = 'http://localhost:8080/oauth2/authorization/google';
  };

  return (
    <button onClick={handleGoogleLogin}>
      Login with Google
    </button>
  );
}
```

---

## 📡 What's Sent?

```http
GET http://localhost:8080/oauth2/authorization/google
```

That's it! Just a simple GET request to YOUR backend, not Google (yet).

---

## 🤔 Wait, Why Backend? Why Not Directly to Google?

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    WHY THROUGH BACKEND?                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   OPTION A: React → Google directly ❌                                       │
│   ──────────────────────────────────                                         │
│   • React would need to know client_id (OK, it's public)                     │
│   • React would need to build authorization URL                              │
│   • Works for SPAs, but our app has a backend!                               │
│   • Tokens would come to React (less secure)                                 │
│                                                                              │
│   OPTION B: React → Backend → Google ✅ (OUR APPROACH)                       │
│   ───────────────────────────────────                                        │
│   • React just says "start OAuth"                                            │
│   • Backend builds the authorization URL                                     │
│   • Backend has client_secret for later                                      │
│   • Tokens stay on backend (more secure!)                                    │
│                                                                              │
│   This is called BFF (Backend For Frontend) pattern!                         │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔧 What Backend Does (Spring Security Magic)

When Spring Security receives `/oauth2/authorization/google`:

```java
// Spring does this internally:
// 1. Find the "google" client registration from application.yaml
// 2. Build the authorization URL
// 3. Redirect user's browser to Google

// You just configured this:
spring:
  security:
    oauth2:
      client:
        registration:
          google:  // ← This "google" matches the URL!
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: openid, email, profile
```

---

## 📊 Request/Response Summary

| Aspect | Value |
|--------|-------|
| **Who initiates** | User (by clicking button) |
| **Request from** | React UI (browser) |
| **Request to** | Spring Boot backend |
| **Method** | GET |
| **URL** | `/oauth2/authorization/google` |
| **Response** | 302 Redirect to Google |

---

## 💡 Key Insight

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│   Step 1 is the SIMPLEST step!                                               │
│                                                                              │
│   • User clicks button                                                       │
│   • React redirects to backend                                               │
│   • Backend will handle the rest                                             │
│                                                                              │
│   The actual OAuth magic starts in Step 2!                                   │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🤔 Beginner Check

1. What URL does React redirect to?
2. Is this URL going to Google or your backend?
3. What does "google" in the URL match with?

Answers:

1. `http://localhost:8080/oauth2/authorization/google`
2. Your backend (Spring Boot at :8080)
3. The registration name in `application.yaml`

---

**Next:** [05_Step2_Authorization_Request.md](./05_Step2_Authorization_Request.md)
