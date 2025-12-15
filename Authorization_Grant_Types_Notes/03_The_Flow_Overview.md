# 03 - The Flow Overview: Bird's Eye View

> 📌 Before diving into details, see the complete picture!

---

## 🎯 The 6-Step Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    AUTHORIZATION CODE GRANT - 6 STEPS                        │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   STEP 1: User clicks "Login with Google"                                    │
│   ─────────────────────────────────────────                                  │
│   User → Your App: "I want to login!"                                        │
│                                                                              │
│   STEP 2: Your App redirects to Google                                       │
│   ─────────────────────────────────────                                      │
│   Your App → User's Browser: "Go to Google for login"                        │
│   Browser redirects to: accounts.google.com/...                              │
│                                                                              │
│   STEP 3: User authenticates at Google                                       │
│   ────────────────────────────────────                                       │
│   User → Google: "Here's my password"                                        │
│   Google: "Valid! What access do you approve?"                               │
│   User: "Yes, I approve!"                                                    │
│                                                                              │
│   STEP 4: Google redirects back with Authorization Code                      │
│   ──────────────────────────────────────────────────────                     │
│   Google → Browser → Your App: "Here's the code: abc123"                     │
│   (Code is temporary, one-time use!)                                         │
│                                                                              │
│   STEP 5: Your App exchanges Code for Tokens (BACKEND!)                      │
│   ─────────────────────────────────────────────────────                      │
│   Your App → Google: "Here's code + my client_secret"                        │
│   Google → Your App: "Here's access_token + id_token!"                       │
│   (This happens server-to-server, user doesn't see!)                         │
│                                                                              │
│   STEP 6: Your App uses the Tokens                                           │
│   ──────────────────────────────────                                         │
│   Your App → Resource Server: "Here's my token, give me data!"               │
│   Resource Server: "Token valid! Here's the data!"                           │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🎨 Visual Flow Diagram

```
    USER                 YOUR APP                GOOGLE                RESOURCE
   (Browser)            (Backend)            (Auth Server)             SERVER
      │                     │                      │                      │
      │                     │                      │                      │
      │ 1. Click Login      │                      │                      │
      │────────────────────►│                      │                      │
      │                     │                      │                      │
      │ 2. Redirect to Google                      │                      │
      │◄────────────────────│                      │                      │
      │─────────────────────────────────────────── ►                      │
      │                     │                      │                      │
      │                     │  3. User Login +     │                      │
      │                     │     Consent          │                      │
      │◄──────────────────────────────────────────│                      │
      │ (login page)        │                      │                      │
      │──────────────────────────────────────────►│                      │
      │ (credentials)       │                      │                      │
      │                     │                      │                      │
      │ 4. Redirect with code                      │                      │
      │◄──────────────────────────────────────────│                      │
      │─────────────────────►                      │                      │
      │                     │                      │                      │
      │                     │ 5. Code + Secret     │                      │
      │                     │─────────────────────►│                      │
      │                     │                      │                      │
      │                     │ access_token+id_token│                      │
      │                     │◄─────────────────────│                      │
      │                     │                      │                      │
      │                     │ 6. Request with Token│                      │
      │                     │──────────────────────────────────────────── ►
      │                     │                      │                      │
      │                     │              Protected Data                 │
      │                     │◄─────────────────────────────────────────────
      │                     │                      │                      │
      │ User sees dashboard │                      │                      │
      │◄────────────────────│                      │                      │
      │                     │                      │                      │
```

---

## 📊 What Travels Where?

| Step | What's Sent | How | Security |
|------|-------------|-----|----------|
| 1 | Login request | Browser click | Normal |
| 2 | Redirect URL with client_id, scope | URL (Front Channel) | Public |
| 3 | User's Google password | HTTPS to Google | Google handles |
| 4 | Authorization Code | URL (Front Channel) | ⚠️ One-time use |
| 5 | Code + client_secret | HTTPS POST (Back Channel) | 🔐 SECRET! |
| 5b | access_token + id_token | Response (Back Channel) | 🔐 Never in browser! |
| 6 | Token to Resource Server | Authorization header | Bearer token |

---

## 💡 Key Concept: Front Channel vs Back Channel

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    TWO CHANNELS                                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   FRONT CHANNEL (Through Browser):                                           │
│   ─────────────────────────────────                                          │
│   • Steps 1, 2, 3, 4                                                         │
│   • User can see the URLs                                                    │
│   • Data in URL query parameters                                             │
│   • ⚠️ Less secure - browser history, extensions can see                     │
│                                                                              │
│   BACK CHANNEL (Server-to-Server):                                           │
│   ────────────────────────────────                                           │
│   • Step 5                                                                   │
│   • User CANNOT see this                                                     │
│   • Direct HTTPS POST between servers                                        │
│   • 🔐 MORE secure - secrets stay hidden!                                    │
│                                                                              │
│   This is WHY Authorization Code Grant is secure!                            │
│   Tokens are exchanged in the BACK CHANNEL!                                  │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## ⭐ Why Authorization "Code" and not just "Token"?

```
Q: Why doesn't Google just send the token in Step 4?

A: Because Step 4 goes through the browser (Front Channel)!
   If token was in the URL:
   → Browser history would have it
   → Browser extensions could steal it
   → Referer headers might leak it
   
   Instead, Google sends a CODE (useless alone).
   The CODE is exchanged for TOKEN in Step 5 (Back Channel).
   Step 5 needs client_secret (which browser doesn't have).
   
   ⭐ CODE = Temporary ticket that only YOUR SERVER can redeem!
```

---

## 🤔 Beginner Check: Quick Quiz

1. How many steps are there in Authorization Code Grant?
2. Which step involves the user entering their password?
3. Which step has the client_secret? (Front or Back channel?)
4. Why is the code not directly a token?

Answers: 6 steps, Step 3, Back channel (Step 5), Security - code alone is useless without client_secret!

---

**Next:** [04_Step1_User_Initiates.md](./04_Step1_User_Initiates.md)
