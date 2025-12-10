Mawa, idi Spring Boot **"Smartness"** (Auto-Configuration) valla jarugutundi.

Simple ga cheppalante: **"Menu lo okkate item unte, Waiter order adagadu, direct ga techestaadu."** 🍛

Deenni 2 Technical Reasons tho break cheddam:

### 1\. The "Secure by Default" Rule 🔒

Spring Security default nature entante: **"Lock Everything."**

  * Nuvvu code lo specific ga cheppakapothe, app lo unna prathi URL (`/`, `/home`, `/anything`) **Protected** ga maripothundi.
  * Nuvvu `http://localhost:8080/` hit chesinappudu, Spring Security Gatekeeper addam vachi: *"Boss, idhi private area. Login aithe tappa lopalki vellalevu"* antundi.

### 2\. The "Single Provider" Shortcut 🚀 (The Main Reason)

Ikkade asalu magic undi.
Normally, Spring Security login page chupinchali (User/Password box). Kani manam **OAuth2 Client** dependency vadam. So username/password box undadu.

Spring Security nee `application.yml` ni scan chestundi:

  * **Check:** "Veedu enni login options ichadu?"
  * **Result:** "Okkate undi -\> **Google**."

**Spring Logic:**
*"Ok, veedi daggara unnade okka option (Google). Malli nenu oka page open chesi, andulo 'Login with Google' ani oka button petti, user tho daanni click cheyinchi... time waste enduku? Direct ga Google ke pampichesta\!"*

Anduke adi ninnu automatically `/oauth2/authorization/google` ki redirect chestundi.

-----

### 🧪 Experiment (To Prove This)

Okavela nuvvu nee `application.yml` lo **GitHub** ni kooda add chesav anuko (Fake credentials tho aina parledu).

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google: ...
          github: # Just fake add chey
            client-id: fake-id
            client-secret: fake-secret
```

**Ippudu Em Jarugutundi?**

1.  Nuvvu `http://localhost:8080/` hit chesthe...
2.  Spring chustundi: "Oh, rendu options unnayi (Google & GitHub)."
3.  **Auto-Redirect Jaragadu\!**
4.  Browser lo oka **Default Login Page** open avtundi using **DefaultLoginPageGeneratingFilter**.
5.  Akkada neeku 2 Links kanipistayi:
      * [ Google ]
      * [ GitHub ]
6.  Nuvvu "Google" click chesthe ne redirect avtundi.

### 🎯 Summary

Nuvvu `localhost:8080` kottinappudu:

1.  Access Denied (Anonymous user).
2.  Spring checks providers.
3.  Only **Google** found.
4.  **Smart Decision:** "Okke dari undi kabatti, auto-redirect chesey."

So, adi bug kadu feature\! Spring manaki user experience smooth ga undadaniki aa extra click ni tagginchesindi. 😎

Clear ah Mawa? Ippudu manam **Resource Server** start cheddama?