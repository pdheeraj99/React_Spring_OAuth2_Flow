# OAuth 2.0 Scopes Explained

## 🎯 What Are Scopes?

Scopes = **Permissions you're requesting from Google**

```yaml
# In application.yaml
scope:
  - openid      # "I want ID Token"
  - email       # "I want email in ID Token"
  - profile     # "I want name, picture in ID Token"
```

---

## 📊 Two Types of Scopes

```
┌─────────────────────────────────────────────────────────────────────────┐
│                     IDENTITY SCOPES                                      │
│                   (Affect ID TOKEN)                                      │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   openid   →  "Generate ID Token for me"                                │
│               Without this = NO ID Token!                                │
│                                                                          │
│   email    →  "Include email in ID Token"                               │
│               ID Token gets: email, email_verified                       │
│                                                                          │
│   profile  →  "Include profile info in ID Token"                        │
│               ID Token gets: name, picture, given_name                   │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                       API SCOPES                                         │
│                 (Affect ACCESS TOKEN)                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   https://www.googleapis.com/auth/drive.readonly                        │
│   →  Access Token can call Google Drive API                             │
│                                                                          │
│   https://www.googleapis.com/auth/gmail.readonly                        │
│   →  Access Token can call Gmail API                                    │
│                                                                          │
│   https://www.googleapis.com/auth/photoslibrary.readonly                │
│   →  Access Token can call Google Photos API                            │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Flow: Scopes → Tokens

```
You request: scope = [openid, email, profile]
                     │
                     ▼
              ┌─────────────────────────────────────────┐
              │           GOOGLE PROCESSES               │
              ├─────────────────────────────────────────┤
              │                                          │
              │  openid?  YES → Create ID Token ✅       │
              │  email?   YES → Add email to token ✅    │
              │  profile? YES → Add name, picture ✅     │
              │                                          │
              │  Always → Create Access Token ✅         │
              │                                          │
              └─────────────────────────────────────────┘
                     │
                     ▼
              Google Returns:
              {
                "access_token": "ya29.xxx",
                "id_token": {
                  "sub": "112416...",      ← Always
                  "email": "x@gmail.com",  ← Because email scope
                  "name": "Dheeraj",       ← Because profile scope
                  "picture": "https://..." ← Because profile scope
                }
              }
```

---

## 📋 Complete Scope Reference

| Scope | Token Affected | What You Get |
|-------|----------------|--------------|
| `openid` | ID Token | Creates the ID Token itself |
| `email` | ID Token | `email`, `email_verified` claims |
| `profile` | ID Token | `name`, `picture`, `given_name` claims |
| `drive` | Access Token | Can call Drive API |
| `gmail` | Access Token | Can call Gmail API |
| `calendar` | Access Token | Can call Calendar API |

---

## 💡 Key Insight

```
Q: "ID Token lo aa details kavali ani ekkada mention chestam?"
A: SCOPES lo!

scope: [openid]                    → ID Token with only "sub"
scope: [openid, email]             → ID Token with "sub" + "email"
scope: [openid, email, profile]    → ID Token with "sub" + "email" + "name" + "picture"
```

---

## ⚠️ What Happens If No Scopes?

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    WITHOUT openid SCOPE                                  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│   scope: [email, profile]   (NO openid!)                                │
│                                                                          │
│   Google Returns:                                                        │
│   {                                                                      │
│     "access_token": "ya29.xxx",    ← ✅ Always given                    │
│     "id_token": null               ← ❌ NOT given! (no openid)          │
│   }                                                                      │
│                                                                          │
│   WITHOUT openid = NO ID TOKEN!                                          │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

### Visual Comparison:

```
scope: []  (empty or no openid)
    │
    └──▶ Google returns: { access_token: "ya29.xxx" }
                         (NO id_token!)

scope: [openid]
    │
    └──▶ Google returns: { access_token: "ya29.xxx", id_token: "eyJ..." }
                         (id_token has only "sub")

scope: [openid, email, profile]
    │
    └──▶ Google returns: { access_token: "ya29.xxx", id_token: "eyJ..." }
                         (id_token has sub + email + name + picture)

scope: [openid, email, profile, drive.readonly]
    │
    └──▶ Google returns: { access_token: "ya29.xxx", id_token: "eyJ..." }
                         (id_token same as above)
                         (access_token can now call Drive API!)
```

---

## 📝 How to Include API Scopes in Configuration

**Both Identity and API scopes go in the SAME `scope` configuration!**

```yaml
# application.yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: xxx
            client-secret: xxx
            scope:
              # ─────────────────────────────────────
              # IDENTITY SCOPES (affect ID Token)
              # ─────────────────────────────────────
              - openid                     # Creates ID Token
              - email                      # Adds email to ID Token
              - profile                    # Adds name, picture to ID Token
              
              # ─────────────────────────────────────
              # API SCOPES (affect Access Token)
              # ─────────────────────────────────────
              - https://www.googleapis.com/auth/drive.readonly
              - https://www.googleapis.com/auth/gmail.readonly
              - https://www.googleapis.com/auth/photoslibrary.readonly
```

### Summary Table:

| Scope | Where Goes | Effect |
|-------|------------|--------|
| `openid` | - | Enables ID Token |
| `email`, `profile` | ID Token | Adds claims to ID Token |
| `drive`, `gmail`, etc. | Access Token | Allows calling those APIs |

**All scopes go in the same `scope:` list!** 👍

---

## 🛡️ Security: Consent Screen

When you request scopes, Google shows user exactly what you're asking:

```
┌─────────────────────────────────────────────────────────────────┐
│                     Google Consent Screen                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   "PhotoVault Pro" wants to:                                    │
│                                                                  │
│   ✓ See your email address (email scope)                        │
│   ✓ See your personal info (profile scope)                      │
│                                                                  │
│   If app also requested drive scope:                            │
│   ⚠️ View all files in Google Drive (drive scope)               │
│                                                                  │
│              [Allow]          [Deny]                             │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🎨 For Excalidraw Diagram

```
IDENTITY SCOPES                    API SCOPES
     │                                  │
     ▼                                  ▼
┌─────────┐                      ┌─────────────┐
│ openid  │──┐                   │   drive     │──┐
│ email   │──┼──▶ ID TOKEN       │   gmail     │──┼──▶ ACCESS TOKEN
│ profile │──┘    (JWT)          │   photos    │──┘    (ya29.xxx)
└─────────┘       │              └─────────────┘       │
                  ▼                                    ▼
            User Identity                      Call Google APIs
            (who you are)                      (what you access)
```
