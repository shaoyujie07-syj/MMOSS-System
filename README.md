# FIT5136 Assignment 3 – MMOSS System (Final Version)

## 🏪 Project Overview
The **Monash Multi-Option Shopping System (MMOSS)** is a complete e-commerce simulation system that enables users to register, log in, browse products, manage carts, make purchases, apply promo codes, and maintain memberships.  
This **final version** includes all features from the system flowchart and represents the complete implementation of MMOSS.

---

## 🧱 Project Structure
```
MMOSS/
├── data/
│   ├── users.csv, accounts.csv, memberships.csv
│   ├── products.csv, stores.csv, promos.csv
│   ├── orders.csv, order_items.csv, payments.csv
│
├── src/
│   └── edu/monash/
│       ├── app/ Main.java
│       ├── domain/
│       │   ├── User, Customer, Administrator
│       │   ├── VIPMembership, Product, CartItem, ShoppingCart
│       │   ├── Order, OrderLine, Store, PromoCode
│       ├── repo/
│       │   ├── UsersRepo, AccountsRepo, MembershipsRepo
│       │   ├── ProductsRepo, StoresRepo, PromosRepo
│       │   ├── OrdersRepo, PaymentsRepo, CsvUtil
│       ├── service/
│       │   ├── AuthService, ProfileService, CatalogService, CartService
│       │   ├── PricingService, PromoService, CheckoutService
│       │   ├── AccountService, MembershipService, AdminService
│       └── usecase/
│           ├── RegisterUser, LoginUser, ViewProfile
└── README.md
```

---

## ✨ Implemented Features

### ✅ Core Functionalities
| Module | Description |
|---------|--------------|
| **User Management** | Register and log in using Monash student emails (`@student.monash.edu` or `@monash.edu`); password must contain an uppercase letter and digit. |
| **Profile & Membership** | View profile, balance, and membership status; purchase, renew, and cancel memberships. |
| **Catalog & Products** | Browse, search, and filter products by category, brand, and keyword. |
| **Shopping Cart** | Add, remove, and clear items; limit: 20 distinct products and 10 units per product. |
| **Checkout Process** | Supports `Pickup` and `Delivery`; applies fees, student discounts, and promo codes; updates stock and balance. |
| **Promo Code System** | Handles all promo types (`PROMO10`, `FIRST_PICKUP`, `NEWMONASH20`), with logic for first-order restrictions. |
| **Admin Panel** | Add, edit, and delete products; data persistence to `products.csv`. |
| **Order & Payment Records** | All transactions saved automatically to CSV (`orders.csv`, `payments.csv`). |

---

## 🔑 Admin Account
| Email | Password |
|--------|-----------|
| `admin@monash.edu` | `Monash1234!` |

---

## 🧩 Class Diagram Overview

| Layer | Classes |
|-------|----------|
| **Domain** | User, Customer, Administrator, Product, CartItem, ShoppingCart, Order, OrderLine, VIPMembership, Store, PromoCode |
| **Repo** | UsersRepo, AccountsRepo, MembershipsRepo, ProductsRepo, StoresRepo, PromosRepo, OrdersRepo, PaymentsRepo |
| **Service** | AuthService, ProfileService, CatalogService, CartService, PricingService, PromoService, CheckoutService, AccountService, MembershipService, AdminService |
| **Usecase** | RegisterUser, LoginUser, ViewProfile |
| **App** | Main |

**Main Relationships**
- `Customer` → `User` (extends)
- `ShoppingCart` o-- `CartItem`
- `Order` o-- `OrderLine`
- `ProductsRepo` → `Product` (persists)
- `CheckoutService` → `AccountsRepo`, `OrdersRepo`, `PaymentsRepo`, `PromoService`, `PricingService`, `ProfileService`
- `Main` → all Usecases & Services

---

## 🔄 Data Persistence Rules
| CSV File | Purpose |
|-----------|----------|
| `users.csv` | Registered users |
| `accounts.csv` | Balance per user |
| `memberships.csv` | Membership details and expiry |
| `products.csv` | Product catalog |
| `stores.csv` | Store list for pickup |
| `promos.csv` | Promo code definitions |
| `orders.csv` | Order summaries |
| `order_items.csv` | Order details |
| `payments.csv` | Payment records |

---

## 👥 Team Roles

| Member | Role | Main Responsibility | Key Files |
|--------|------|---------------------|------------|
| **A** | User Management & Profile | Implemented registration, login, and profile modules including validation logic and balance initialization. | `AuthService.java`, `RegisterUser.java`, `LoginUser.java`, `ViewProfile.java`, `User.java`, `Customer.java` |
| **B** | Catalog & Shopping Cart | Built product browsing, search, and cart functionalities; handled product filtering and stock updates. | `Product.java`, `CatalogService.java`, `CartService.java`, `ShoppingCart.java`, `CartItem.java` |
| **C** | Checkout & Pricing | Developed checkout logic, promo handling, and pricing policies (delivery, pickup, student discounts). | `CheckoutService.java`, `PricingService.java`, `PromoService.java`, `AccountService.java` |
| **D** | Repository & Admin Management | Implemented CSV persistence, admin operations, and system integration testing; prepared documentation. | `UsersRepo.java`, `ProductsRepo.java`, `OrdersRepo.java`, `PaymentsRepo.java`, `AdminService.java`, `Main.java`, `README.md` |

---

