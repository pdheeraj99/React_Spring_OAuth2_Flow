Mawa, idi Spring Security lo unna **"Plan B" (Fallback Mechanism)**.

Nuvvu `application.yml` lo Google details (Client ID/Secret) teesesav anuko, kani `spring-boot-starter-security` dependency alane unte... **App open avtundi, kani Google ki velladu.**

Deenni **"Default Form Login"** antaru. Em jarugutundo chudu:

### 😲 The Scenario (No Config)

Nuvvu Google details delete chesav. Dependencies matram alane unnayi.
User hits `http://localhost:8080/`.

### 🎬 Em Jarugutundi?

1.  **Spring Checks:** "Veedu Login Options emaina ichada (Google/FB)?" -\> **Answer: NO.**
2.  **Spring Security Logic:** "Orey, veedu config ivvaledu ani App ni open ga vadileyalem kada (Unsecure). So, nenu naa **Default Username/Password** system ni activate chestunna."
3.  **Browser Screen:** Direct ga Google ki redirect avvadam manesi, Spring Security **Oka Login Form** (Username & Password box) chupistundi.

### 🔑 Password Ekkada Untundi?

Aa login page lo:

  * **Username:** `user` (Default)
  * **Password:** Nuvvu App start chesinappudu **Console Logs** lo Spring oka password print chestundi.

Log Example:

```text
Using generated security password: 8692740c-6088-4329-8646-443150577529
```

### 🧠 40LPA Engineering Logic

Spring Security Philosophy entante: **"Secure by Default"**.

  * Nuvvu OAuth config ivvakapothe, adi "OAuth pani cheyatledu" ani silent ga undadu.
  * Ventane **"Basic Authentication"** loki maripothundi.
  * App ni epudu **Lock** chese unchutundi unless nuvvu code rasi `permitAll()` (gate lu teruvu) ani cheppe varaku.

### 🧪 Summary Matrix

| Config Status | Behavior when hitting `localhost:8080` |
| :--- | :--- |
| **Only Google** | **Auto-Redirect** to Google Login Page (No extra clicks). |
| **Google + GitHub** | Show **Selection Page** ([Login with Google] / [Login with GitHub]). |
| **No Options** | Show **Default Form** (Username/Password boxes). |

Clear ah Mawa? Spring Security chala strict officer annamata. Config lekapothe patha paddathi (User/Pass) follow aipothadu.

Ippudu manam **Resource Server (Project 2)** start cheddama? Akkada manam tokens ni ela validate cheyalo chuddam\! 🚀