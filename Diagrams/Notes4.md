**Stateful Architecture** lo pedda scalability bottleneck. Let's break it down technically.

### 1. Sessions Ekkada Store Avtayi? (The Location)

By default, Spring Boot application run ayyetappudu, Sessions ni **Server RAM (JVM Heap Memory)** lo store chestundi.

* **Data Structure:** Lopala idi oka pedda `ConcurrentHashMap` laga pani chestundi.
    * **Key:** Session ID (Ex: `JSESSIONID`)
    * **Value:** User Object (SecurityContext, User Details, Attributes)

### 2. 500 Users vs Memory Explosion (The Calculation)

* **Small Scale (500 Users):**
    * Oka Session object size approx: **2 KB to 4 KB**.
    * 500 Users * 4 KB = **2 MB**.
    * So, 500 users ki server RAM lo just 2MB occupy avtundi. Idi chala chinna vishayam.
* **Large Scale (1 Million Users - The 40LPA Problem):**
    * 1,000,000 * 4 KB = **4 GB**.
    * Ikkada problem vastundi. Neeku 4GB RAM unna server unte, adi nindipotundi. Application slow aipoddi leda `OutOfMemoryError` vachi crash avtundi.

**Solution for Production (Redis):**
Real-world lo (Amazon/Netflix range lo), manam Sessions ni server RAM lo pettam. Manam **Redis** (In-memory Cache) ani oka separate database vadutham.
* Spring Boot automatically sessions ni server nundi teesukelli Redis lo save chestundi (`spring-session-data-redis`).
* Appudu server "Stateless" ga feel avtundi, load taggutundi.

---

### 3. 10 Users Request pedite "Veedu Evaru?" ani Server ki ela telustundi? (The Identification)

Server ki kallu levu, kani dhaaniki **Cookies** chadavadam vachu. Deenne **Session Management Handshake** antaru.

Imagine Server daggara oka **Register (Map)** undi.

**Scenario:**
* **User A** (Browser A) sends request.
* **User B** (Browser B) sends request.

**Step-by-Step Identification:**

1.  **First Meeting (No ID):**
    * **User A** first time login ayyadu. Request lo em identity ledu.
    * **Server:** Validate chesi, oka random **Token** create chestundi. Let's say `ID: 111`.
    * Server Register lo rastundi: `111 = User A`.
    * **Response:** Server User A ki response istu, oka header pedutundi: `Set-Cookie: JSESSIONID=111`.

2.  **Browser's Job:**
    * **User A Browser:** Aa cookie (`111`) ni safe ga dachipettukuntundi.

3.  **Second Request (The Identification):**
    * Ippudu **User A** "Profile Page" adigadu.
    * Browser automatic ga aa request tho patu aa cookie ni attach chestundi: `Cookie: JSESSIONID=111`.
    * At the same time, **User B** login ayyi `ID: 222` techukunnadu. Vaadu request pedithe `Cookie: JSESSIONID=222` veltundi.

4.  **Server Lookup:**
    * Server request receive cheskogane, first **Cookie Header** check chestundi.
    * "Oh, veedu `111` techadu." -> Checks Register -> "Ok, `111` ante **User A**." -> Serves User A's data.
    * "Veedu `222` techadu." -> Checks Register -> "Ok, `222` ante **User B**." -> Serves User B's data.



### Summary

* **Storage:** Default ga **RAM (JVM Heap)**. Production lo **Redis**.
* **Identification:** Via **Cookies (`JSESSIONID`)**. Cookie lekapothe Server ki nuvvu evaro teliyadu (prathi request kotha user laage anipistundi).

**Why JWT? (Connection to next topic):**
Nuvvu adigina "Memory Perigipotundi kada?" ane bhayam vallane manam **JWT (JSON Web Token)** ki veltunnam.
* **Session:** Server memory lo data dachukuntundi (Stateful).
* **JWT:** Data antha Token lone encrypt chesi User ki ichestundi. Server em dachukodu (Stateless). Memory issue solved!