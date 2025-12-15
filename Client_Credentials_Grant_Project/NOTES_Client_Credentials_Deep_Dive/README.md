# 📚 Client Credentials Grant - Complete Deep Dive Notes

## Ee Notes lo Emi Undi?

| File | Topic | Description |
|------|-------|-------------|
| [01_Overview.md](./01_Overview.md) | Introduction | Client Credentials enti, enduku vadtaam |
| [02_Architecture.md](./02_Architecture.md) | Architecture | 3 services, ports, roles |
| [03_Request_Flow.md](./03_Request_Flow.md) | Complete Flow | Step-by-step requests & responses |
| [04_JWT_Deep_Dive.md](./04_JWT_Deep_Dive.md) | JWT | Structure, Base64, signing |
| [05_Cryptography_Basics.md](./05_Cryptography_Basics.md) | Crypto | Symmetric, Asymmetric, Public/Private Keys |
| [06_Code_Walkthrough.md](./06_Code_Walkthrough.md) | Code | Line-by-line explanation |
| [07_Common_Doubts.md](./07_Common_Doubts.md) | FAQs | All doubts discussed with explanations |

---

## 🎯 Quick Reference

**Project Location:** `Client_Credentials_Grant_Project/Option1_Spring_Auth_Server/`

**Services:**

- Auth Server: `localhost:9000`
- Order-Service: `localhost:8080`
- Inventory-Service: `localhost:8081`

**Test Command:**

```bash
curl http://localhost:8080/api/orders/check-stock/laptop-001
```

---

## 💡 Key Takeaway (One Line)

> **Client Credentials = App credentials tho token teeskoni, oka service inko service ni call chestundi. User involved kaadu!**

---

*Last Updated: December 15, 2025*
