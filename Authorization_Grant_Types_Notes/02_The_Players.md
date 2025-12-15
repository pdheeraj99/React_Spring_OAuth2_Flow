# 02 - The Players: Who's Involved?

> 📌 OAuth 2.0 has 4 main actors. Know them before understanding the flow!

---

## 🎭 Meet the 4 Players

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    THE 4 OAUTH ACTORS                                        │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   1. RESOURCE OWNER (The User)                                               │
│      ─────────────────────────                                               │
│      👤 The human who owns the data                                          │
│      Example: You, who owns your Google Photos                               │
│                                                                              │
│   2. CLIENT (Your Application)                                               │
│      ──────────────────────────                                              │
│      💻 The app that wants to access user's data                             │
│      Example: PhotoBackup app, or our Spring Boot app                        │
│                                                                              │
│   3. AUTHORIZATION SERVER (The Gatekeeper)                                   │
│      ─────────────────────────────────────                                   │
│      🔐 Issues tokens after verifying user identity                          │
│      Example: accounts.google.com                                            │
│                                                                              │
│   4. RESOURCE SERVER (The Data Holder)                                       │
│      ─────────────────────────────────                                       │
│      📦 Holds the protected data, validates tokens                           │
│      Example: photos.googleapis.com or our Resource Server at :8081          │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🎨 Visual: Where Each Player Lives

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                                                              │
│                         ┌───────────────────┐                                │
│                         │  RESOURCE OWNER   │                                │
│                         │      (User)       │                                │
│                         │   👤 Dheeraj      │                                │
│                         └─────────┬─────────┘                                │
│                                   │                                          │
│           Uses browser to         │                                          │
│           interact with           │                                          │
│                                   ▼                                          │
│   ┌───────────────────────────────────────────────────────────────────┐      │
│   │                                                                   │      │
│   │    ┌─────────────────┐                 ┌─────────────────┐        │      │
│   │    │     CLIENT      │                 │  AUTHORIZATION  │        │      │
│   │    │  (Your App)     │ ◄─────────────► │     SERVER      │        │      │
│   │    │                 │   Tokens        │   (Google)      │        │      │
│   │    │  Spring Boot    │                 │                 │        │      │
│   │    │  :8080          │                 │  accounts.      │        │      │
│   │    └────────┬────────┘                 │  google.com     │        │      │
│   │             │                          └─────────────────┘        │      │
│   │             │                                                     │      │
│   │             │ Tokens                                              │      │
│   │             ▼                                                     │      │
│   │    ┌─────────────────┐                                            │      │
│   │    │    RESOURCE     │                                            │      │
│   │    │     SERVER      │                                            │      │
│   │    │                 │                                            │      │
│   │    │  Your API :8081 │                                            │      │
│   │    │  OR Google APIs │                                            │      │
│   │    └─────────────────┘                                            │      │
│   │                                                                   │      │
│   │                        THE INTERNET                               │      │
│   └───────────────────────────────────────────────────────────────────┘      │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📋 Detailed Player Breakdown

### 1. Resource Owner (User) 👤

| Aspect | Description |
|--------|-------------|
| **Who** | The human being (you, me, any user) |
| **Owns** | The protected data (photos, emails, profile info) |
| **Does** | Approves or denies access requests |
| **Where** | At their browser |

```
Example: Dheeraj wants to use PhotoBackup App.
Dheeraj OWNS his Google Photos.
Dheeraj must APPROVE PhotoBackup's request to access his photos.
```

---

### 2. Client (Your Application) 💻

| Aspect | Description |
|--------|-------------|
| **Who** | Your application (web app, mobile app, etc.) |
| **Wants** | Access to user's protected resources |
| **Has** | client_id and client_secret (credentials from Google) |
| **Where** | Your server (Spring Boot at :8080) |

```
Example: PhotoBackup App (Spring Boot)
Has: client_id from Google Cloud Console
Wants: Access to user's Google Photos
```

⚠️ **Important:** The word "Client" in OAuth means YOUR APPLICATION, not the user!

---

### 3. Authorization Server (The Gatekeeper) 🔐

| Aspect | Description |
|--------|-------------|
| **Who** | Google, Facebook, GitHub, or any OAuth provider |
| **Does** | Authenticates users, issues tokens |
| **Checks** | User credentials, consent, scopes |
| **URL** | accounts.google.com (for Google) |

```
Example: Google's Authorization Server
Located at: https://accounts.google.com
Does: Verifies user's Google password
Issues: Authorization Codes, Access Tokens, ID Tokens
```

---

### 4. Resource Server (The Data Holder) 📦

| Aspect | Description |
|--------|-------------|
| **Who** | Server that holds protected data |
| **Has** | User's actual data (photos, emails, etc.) |
| **Does** | Validates tokens, returns data if valid |
| **Where** | photos.googleapis.com OR your own API at :8081 |

```
Two types of Resource Servers:

1. GOOGLE'S RESOURCE SERVER:
   URL: photos.googleapis.com
   Has: User's Google Photos
   Validates: Google's access_token

2. YOUR RESOURCE SERVER:
   URL: localhost:8081
   Has: Your app's protected APIs
   Validates: Google's id_token (JWT)
```

---

## 💡 Real-World Analogy: Hotel Check-in

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    HOTEL ANALOGY                                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│   RESOURCE OWNER = Guest (You)                                               │
│   → You own the booking, you decide who enters your room                     │
│                                                                              │
│   CLIENT = Your Friend                                                       │
│   → Wants to enter your hotel room                                           │
│                                                                              │
│   AUTHORIZATION SERVER = Front Desk                                          │
│   → Verifies your identity, issues room key cards                            │
│   → "Mr. Dheeraj, do you want to give your friend a key card?"               │
│                                                                              │
│   RESOURCE SERVER = Hotel Room Door                                          │
│   → Has your belongings (protected resources)                                │
│   → Only opens if valid key card is presented                                │
│                                                                              │
│   FLOW:                                                                      │
│   1. Friend asks you for room access                                         │
│   2. You go to front desk with friend                                        │
│   3. Front desk verifies YOUR identity                                       │
│   4. Front desk asks: "Give friend access to room?"                          │
│   5. You say: "Yes, but only for 1 hour!"                                    │
│   6. Front desk gives friend a LIMITED key card                              │
│   7. Friend uses key card to enter room                                      │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🤔 Beginner Check: Did You Understand?

| Question | Your Answer |
|----------|-------------|
| Who is the Resource Owner in "Login with Google"? | |
| What does the Client have that identifies it to Google? | |
| What does the Authorization Server issue? | |
| What does the Resource Server validate? | |

Answers:

1. Resource Owner = The user (you!)
2. Client has = client_id and client_secret
3. Authorization Server issues = Tokens (code, access_token, id_token)
4. Resource Server validates = Tokens before returning data

---

**Next:** [03_The_Flow_Overview.md](./03_The_Flow_Overview.md)
