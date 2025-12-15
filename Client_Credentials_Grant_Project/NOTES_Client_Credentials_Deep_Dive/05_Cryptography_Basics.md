# 🔐 05. Cryptography Basics - Keys & Signing

## 🤔 Problem Statement

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║  PROBLEM:                                                          ║
║  ─────────                                                         ║
║  Inventory-Service gets a JWT token.                               ║
║  How does it KNOW this token was created by Auth-Server?           ║
║  What if an attacker created a fake token?                         ║
║                                                                    ║
║  SOLUTION:                                                         ║
║  ──────────                                                        ║
║  DIGITAL SIGNATURES using Public/Private Key cryptography!         ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

---

## 📖 Two Types of Cryptography

### 1️⃣ SYMMETRIC (Same Key)

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║  SYMMETRIC = ONE KEY for both locking and unlocking                ║
║                                                                    ║
║  Example: Your house key                                           ║
║  • Same key locks the door                                         ║
║  • Same key unlocks the door                                       ║
║                                                                    ║
║  In Computer:                                                      ║
║  ─────────────                                                     ║
║  KEY = "my-secret-123"                                             ║
║                                                                    ║
║  Encrypt: "Hello" + KEY → "xK#9mL"                                 ║
║  Decrypt: "xK#9mL" + SAME KEY → "Hello"                            ║
║                                                                    ║
║  PROBLEM:                                                          ║
║  ─────────                                                         ║
║  If I want to send encrypted message to you...                     ║
║  How do I give you the key safely?                                 ║
║  If attacker intercepts key → All messages exposed!                ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

### 2️⃣ ASYMMETRIC (Two Different Keys) ← JWT uses this

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║  ASYMMETRIC = TWO KEYS that work together                          ║
║                                                                    ║
║  🔐 PRIVATE KEY: Keep SECRET (never share!)                        ║
║  🔓 PUBLIC KEY:  Share with EVERYONE                               ║
║                                                                    ║
║  These keys are MATHEMATICALLY linked!                             ║
║  But you CANNOT derive one from the other!                         ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

---

## 🔒 Padlock Analogy (Best Analogy!)

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║  Imagine a SPECIAL PADLOCK with TWO DIFFERENT KEYS:                ║
║                                                                    ║
║  ┌─────────────────────────────────────────────────────────────┐   ║
║  │                                                             │   ║
║  │  🔑 KEY A (PUBLIC):   Can only LOCK the padlock             │   ║
║  │  🗝️ KEY B (PRIVATE):  Can only UNLOCK the padlock           │   ║
║  │                                                             │   ║
║  │  If you have Key A... you CANNOT unlock!                    │   ║
║  │  If you have Key B... you CAN unlock!                       │   ║
║  │                                                             │   ║
║  └─────────────────────────────────────────────────────────────┘   ║
║                                                                    ║
║  Scenario:                                                         ║
║  ──────────                                                        ║
║  Step 1: You give KEY A (public) to everyone                       ║
║          "Here, anyone can have this!"                             ║
║                                                                    ║
║  Step 2: Friend puts gift in box, locks with KEY A                 ║
║          Box is now LOCKED!                                        ║
║                                                                    ║
║  Step 3: Friend sends locked box to you                            ║
║          Attacker sees box... but has only KEY A                   ║
║          KEY A cannot UNLOCK... only LOCK!                         ║
║          Attacker is stuck! ❌                                      ║
║                                                                    ║
║  Step 4: You open with YOUR KEY B (private)                        ║
║          Only YOU have KEY B!                                      ║
║          Box opens! ✅                                              ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

---

## 🔄 Two Use Cases for Same Keys

### Same key pair can be used for TWO different purposes

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║  USE CASE 1: ENCRYPTION (Hide Message)                             ║
║  ─────────────────────────────────────                             ║
║                                                                    ║
║  Goal: Only receiver should read                                   ║
║                                                                    ║
║  PUBLIC KEY  → Encrypts (anyone can lock)                          ║
║  PRIVATE KEY → Decrypts (only owner can unlock)                    ║
║                                                                    ║
╠════════════════════════════════════════════════════════════════════╣
║                                                                    ║
║  USE CASE 2: SIGNING (Prove Identity) ← JWT uses THIS!             ║
║  ─────────────────────────────────────                             ║
║                                                                    ║
║  Goal: Prove who created the document                              ║
║                                                                    ║
║  PRIVATE KEY → Signs (only owner can create signature)             ║
║  PUBLIC KEY  → Verifies (anyone can check if signature is real)    ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

---

## 🖊️ Signature Analogy (PM Seal Example)

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║  PRIME MINISTER'S OFFICIAL DOCUMENT                                ║
║                                                                    ║
║  Document: "Release 100 crores for flood relief"                   ║
║                                                                    ║
║  How do we PROVE this is REAL official document?                   ║
║                                                                    ║
║  → PRIME MINISTER'S OFFICIAL SEAL! 🔴                               ║
║                                                                    ║
║  ┌────────────────────────────────────────────────────────────┐    ║
║  │                                                            │    ║
║  │  SEAL RING (Private Key) = Only PM has the actual ring     │    ║
║  │  → Used to CREATE real seals                               │    ║
║  │                                                            │    ║
║  │  SEAL PHOTO (Public Key) = Everyone knows what seal looks  │    ║
║  │  → Used to VERIFY if seal is real                          │    ║
║  │                                                            │    ║
║  └────────────────────────────────────────────────────────────┘    ║
║                                                                    ║
║  Verification Flow:                                                ║
║  ──────────────────                                                ║
║  1. PM SIGNS document with his SEAL RING (Private Key)             ║
║  2. Document sent to bank                                          ║
║  3. Bank COMPARES seal with known photo (Public Key)               ║
║     "Is this PM's real seal?"                                      ║
║  4. ✅ Match → Release money                                        ║
║     ❌ No match → Reject, it's fake!                                ║
║                                                                    ║
║  KEY INSIGHT:                                                      ║
║  ─────────────                                                     ║
║  Seeing the seal photo (Public Key) ≠ Having the ring!             ║
║  Attacker can SEE what seal looks like...                          ║
║  But CANNOT create fake seal without the RING!                     ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

---

## 💵 Currency Note Analogy

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║  ₹500 NOTE                                                         ║
║                                                                    ║
║  DATA: "Five Hundred Rupees"                                       ║
║  SIGNATURE: RBI Governor's signature                               ║
║                                                                    ║
║  PRIVATE KEY = RBI's special printing plates                       ║
║                Only RBI has these!                                 ║
║                                                                    ║
║  PUBLIC KEY = What real note looks like                            ║
║               Everyone knows the features, watermarks              ║
║                                                                    ║
║  Can YOU print ₹500 note? ❌ NO!                                    ║
║  You don't have the printing plates (private key)!                 ║
║                                                                    ║
║  Can you VERIFY a note is real? ✅ YES!                             ║
║  You know what real note looks like (public key)!                  ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

---

## 🎯 Applied to JWT

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║  AUTH-SERVER = Prime Minister / RBI                                ║
║  PRIVATE KEY = PM's seal ring / RBI printing plates                ║
║  PUBLIC KEY  = Seal photo / Note features                          ║
║  JWT         = Official document / Currency note                   ║
║  SIGNATURE   = Seal imprint / Security features                    ║
║                                                                    ║
║  FLOW:                                                             ║
║  ─────                                                             ║
║  1. Auth-Server SIGNS JWT with PRIVATE KEY                         ║
║     "I (Auth-Server) authorize 'order-service' to access inventory"║
║                                                                    ║
║  2. JWT sent to Order-Service                                      ║
║                                                                    ║
║  3. Order-Service sends JWT to Inventory-Service                   ║
║                                                                    ║
║  4. Inventory-Service VERIFIES with PUBLIC KEY                     ║
║     "Is this really Auth-Server's signature?"                      ║
║                                                                    ║
║  5. ✅ Valid → Allow access!                                        ║
║     ❌ Invalid → Reject!                                            ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

---

## 🔐 Where Are Keys in Our Code?

### Auth-Server (Has BOTH keys)

```java
// AuthorizationServerConfig.java

@Bean
public JWKSource<SecurityContext> jwkSource() {
    KeyPair keyPair = generateRsaKey();  // ← Creates BOTH keys!
    RSAPublicKey publicKey = keyPair.getPublic();   // ← PUBLIC
    RSAPrivateKey privateKey = keyPair.getPrivate(); // ← PRIVATE
    
    // Private key stays here, used for signing
    // Public key exposed via /oauth2/jwks endpoint
}

private static KeyPair generateRsaKey() {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);  // 2048-bit key (secure!)
    return generator.generateKeyPair();
}
```

### Inventory-Service (Has only PUBLIC key)

```yaml
# application.yaml

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:9000
          # ↑ Spring automatically fetches PUBLIC KEY from here!
          # At startup, calls: http://localhost:9000/oauth2/jwks
```

---

## 🛡️ Security Questions

### Q: If attacker gets PUBLIC KEY, can they create fake tokens?

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║  ❌ NO! Public key can only VERIFY, not SIGN!                       ║
║                                                                    ║
║  Attacker has: Public Key ✓                                        ║
║  Attacker needs: Private Key ✗ (never exposed!)                    ║
║                                                                    ║
║  Like having a PHOTO of PM's seal...                               ║
║  But NOT having the actual SEAL RING!                              ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

### Q: If attacker steals the TOKEN, can they use it?

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║  ⚠️ YES! Token theft is a real risk!                                ║
║                                                                    ║
║  PROTECTIONS:                                                      ║
║  ────────────                                                      ║
║  1. Short expiry (1 hour or less)                                  ║
║  2. HTTPS (encrypted transmission)                                 ║
║  3. Limited scope (minimal permissions)                            ║
║  4. Server-to-server = harder to intercept                         ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

---

## 📊 Summary Table

| Concept | Real World | In JWT |
|---------|-----------|--------|
| **Private Key** | PM's seal ring | Auth Server's signing key |
| **Public Key** | Photo of seal | Published at /oauth2/jwks |
| **Signing** | Putting seal | Creating JWT signature |
| **Verifying** | Checking seal | Inventory validates JWT |
| **Token** | Sealed document | JWT (Header.Payload.Signature) |

---

## 🎯 Key Takeaways

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║  1. PRIVATE KEY = Secret! Creates signatures.                      ║
║                   Only Auth-Server has it.                         ║
║                                                                    ║
║  2. PUBLIC KEY = Shared! Verifies signatures.                      ║
║                  Anyone can have it - it's safe!                   ║
║                                                                    ║
║  3. SIGNING = "I created this" (needs private key)                 ║
║                                                                    ║
║  4. VERIFYING = "Is this really from them?" (needs public key)     ║
║                                                                    ║
║  5. Having PUBLIC KEY ≠ Creating fake tokens!                      ║
║                                                                    ║
╚════════════════════════════════════════════════════════════════════╝
```

---

**Next:** [06_Code_Walkthrough.md](./06_Code_Walkthrough.md) - Line-by-line code explanation
