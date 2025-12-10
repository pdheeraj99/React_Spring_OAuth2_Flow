Mawa, simple ga cheppalante: **Adi nee "ID Card"**. 🪪

Technically cheppalante, **User ippude login ayyadu kada, aa User yokka Details (Name, Email, Photo) ni Controller method loki inject cheyadaniki** vaade shortcut idi.

Deenni 3 levels lo break cheddam:

### 1\. The "Hard Way" (Without this annotation) 😫

Okavela nuvvu ee annotation vadakapothe, logged-in user details kavali ante inta pedda code rayalsi vachedi:

```java
// Idi chala patha paddathi (Don't use this)
Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
OAuth2User user = (OAuth2User) authentication.getPrincipal();
String email = user.getAttribute("email");
```

Idi chala peddaga, complex ga undi kada?

### 2\. The "Smart Way" (With `@AuthenticationPrincipal`) 😎

Spring Security developers manakosam ee annotation create chesaru.

  * **Meaning:** "Spring Mawa, `SecurityContext` (Session) lo evadైte user unnado, vaadi details ni direct ga techi `principal` ane variable lo pettu."

So, neeku direct ga **Ready-made Object** vastundi. No headache.

### 3\. Asalu `OAuth2User` ante enti?

Mana case lo, manam **Google** login vadutunnam.

  * Google manaki return lo User Data ni **JSON format** lo istundi (`name`, `email`, `picture`, `sub`).
  * Spring Security aa JSON ni `OAuth2User` ane Java Object ga marchuthundi.
  * Indulo data **Map** format lo untundi (`Key: Value`).

**Example Data inside `principal`:**

```text
Name: "My TV"
Email: "mytv35049@gmail.com"
Attributes: {
    "sub": "100897...",
    "name": "My TV",
    "given_name": "My",
    "picture": "https://lh3.google...",
    "email": "mytv35049@gmail.com",
    "email_verified": true
}
```

-----

### 🧠 40LPA Interview Insight

**Interviewer:** "What is the difference between `Principal` and `Authentication`?"

**Your Answer:**

  * **Authentication:** Idi **Whole File**. Indulo User evaru (`Principal`), vaadi permissions enti (`Authorities`), vaadu login ayyada leda (`Authenticated boolean`) anni untayi.
  * **Principal:** Idi **User Profile** matrame. Only "Who is he?" ane data untundi (Like User Object).

**Diagrammatic View:**

**Summary:**
`@AuthenticationPrincipal OAuth2User principal` ante -\> **"Current Logged-in Google User details ni naku ivvu"** ani artham.

Clear ah Mawa? Ippudu aa `principal.getAttribute("email")` ante enduku panichestundo artham aindha? 🚀