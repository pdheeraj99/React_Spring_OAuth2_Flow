### 1. OAuth Stateless kada? Mari Session Enduku?

**Fact:** OAuth 2.0 Protocol **Stateless** ye.
**Meaning:** Google niku ichina Token lo ne Data antha untundi. Google server ki nee gurinchi memory (state) avasaram ledu. Token chupiste chalu.

**Problem:**
Manam Token ni ekkada dachipettali?
* **Browser lo pedadama? (LocalStorage/Cookies):** **Dangerous!** Hacker JavaScript tho Token ni easy ga dongilistadu (XSS Attack).
* **Safe Place:** Mana **Backend Server (Client App)**.

**Solution (The Hybrid Approach):**
Anduke manam **BFF (Backend for Frontend)** pattern vadutunnam.
1.  **Browser <-> Mana Backend:** Stateful (Session Cookie).
    * Browser ki Token ivvam. Just oka `JSESSIONID` (Reference) istam.
    * Idi safe. Hacker Cookie ni dongilinchina, `HttpOnly` flag valla JavaScript tho access cheyaleru.
2.  **Mana Backend <-> Google/Resource Server:** Stateless (OAuth Token).
    * Backend Google tho matladetappudu Token vadutundi.



**Simple ga:**
* Browser tho matladetappudu **Session**.
* Google tho matladetappudu **Token**.
* Manam aa Token ni Session lo dachipedithe, Browser ki Token expose avvakunda Safe ga untundi.

---

### 2. RAM Problem: 1 Lakh Users vaste Server Crash avtunda?

**Nuvvu Cheppindi 100% Correct.**
By default, Spring Boot sessions ni **RAM (In-Memory)** lo pedutundi.
* **Scenario:** 1 Lakh users login ayyaru.
* **Result:** `OutOfMemoryError`. Server **Bhum** 💥. Crash aipothundi.

**The 40LPA Solution (Redis):**
Real companies lo (Netflix, Uber, Swiggy), manam Sessions ni RAM lo pettam. Manam **Redis** ani oka separate Database vadutham.

* **Setup:** Spring Boot lo just oka dependency add chestam: `spring-session-data-redis`.
* **Magic:**
    1.  User Login avtadu.
    2.  Spring Token ni RAM lo kakunda, **Redis DB** lo save chestundi.
    3.  RAM free ga untundi.
    4.  Okavela 1 Lakh users vachina, Redis handle chestundi (Idi chala fast).
    5.  Server Crash avvadu.



---

### 3. Thread vs Session: Thread ki Session ekkadi nundi vastundi?

Idi asalu "Internal Engineering" question. Super! 🔥

**Step-by-Step Flow inside Spring Boot:**

1.  **Request Entry:**
    * Browser request pampistundi (`GET /photos`).
    * Headers lo `Cookie: JSESSIONID=123` untundi.

2.  **Thread Allocation:**
    * Tomcat Server oka **Thread (Worker)** ni create chesi ee request ki assign chestundi.
    * *Thread:* "Na pani ee request ni process cheyadam."

3.  **The Filter Interception (Connection Point):**
    * Request Controller daggaraki velle mundu, `SessionRepositoryFilter` ani okati untundi.
    * **Action:**
        * Thread aa Cookie (`123`) ni chustundi.
        * Thread **Session Store (Map/Redis)** ki velli: "Orey, `123` ID tho data emaina unda?" ani adugutundi.
        * Store: "Avunu, indulo Google Token undi."

4.  **ThreadLocal (Magic Pocket):**
    * Spring Security aa data ni tiskoni **ThreadLocal** ane temporary pocket lo pedutundi (`SecurityContextHolder`).
    * Ippudu aa Thread ki User evaro telusu, Token ekkada undo telusu.

5.  **Processing:**
    * Controller execute avtundi. Thread aa Token vadukuntundi.

6.  **Cleanup:**
    * Response browser ki vellagane, Thread aa pocket (ThreadLocal) ni **Empty** chesesi, next request kosam ready avtundi.

**Analogy:**
* **Session Store:** Library (Books anni akkada untayi).
* **Cookie:** Book Number.
* **Thread:** Student.
* **Action:** Student (Thread) Library ki velli, Number (Cookie) cheppi, Book (Session Data) techukuntadu. Chadivesaka (Process), Book ni malli Library lo pettesthadu. Student daggara permanent ga em undadu.

**Conclusion:**
* Sessions **Create chestunnam** (Browser security kosam).
* Scalability kosam **Redis** vadutham (RAM issue solved).
* Thread prathi sari Cookie vaadi Session Store nundi data techukuntundi.

Mawa, ippudu ee "Stateless vs Session" confusion poinda?
Manam Token ni Session lo dachipettukuni, bayataki (Browser ki) kanipinchakunda chestunnam. Anthe! 🚀