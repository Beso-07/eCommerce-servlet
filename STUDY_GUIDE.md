# E-Commerce Backend Project — Study Guide

> A complete walkthrough of the project, written to be read end-to-end before
> the discussion. It assumes nothing and ties every concept back to a real
> file in this repo.

---

## Table of Contents
1. [Project Overview](#1-project-overview)
2. [Required Features Checklist](#2-required-features-checklist)
3. [Technologies Used](#3-technologies-used)
4. [Project Architecture](#4-project-architecture)
5. [Folder and Package Structure](#5-folder-and-package-structure)
6. [Database Design](#6-database-design)
7. [Request Lifecycle](#7-request-lifecycle)
8. [Authentication System](#8-authentication-system)
9. [Authorization / Admin System](#9-authorization--admin-system)
10. [Product Management](#10-product-management)
11. [Review / Feedback System](#11-review--feedback-system)
12. [Redis Usage](#12-redis-usage)
13. [Error Handling](#13-error-handling)
14. [Security Concepts in the Project](#14-security-concepts-in-the-project)
15. [Detailed Feature Flows](#15-detailed-feature-flows)
16. [Important Code Snippets Explained](#16-important-code-snippets-explained)
17. [How to Run the Project](#17-how-to-run-the-project)
18. [Manual Testing Guide](#18-manual-testing-guide)
19. [Discussion Preparation](#19-discussion-preparation)
20. [Final Summary](#20-final-summary)

---

## 1. Project Overview

This is a **backend-focused** E-Commerce web application written in Java. The
front-end is plain JSP + Bootstrap — the focus of the course is the server
side: how requests are routed, how the database is accessed, how a user logs
in, how products are cached, and how the layers fit together.

**What problem it solves.** A simple online store: customers can register, log
in, browse products, view a product's details, write and delete their own
reviews. Administrators can add, edit, and delete products from a dashboard.

**Why backend-focused.** All the interesting code is on the server: Servlets,
Filters, Services, DAOs, JDBC, Redis, JWT. The JSPs are deliberately thin —
they iterate over data and render it.

**Features supported.**
- View all products with their reviews (home page).
- View product details with all reviews and the add-review form.
- Sign up, sign in (session + JWT), sign out, delete account.
- Add / edit / delete products (admin only).
- Add / delete own reviews.
- Product caching in Redis.
- Per-IP rate limiting in Redis.
- JWT blacklist on logout (in Redis).
- Centralized error handling with friendly error pages (400 / 403 / 404 / 500).
- Server-side validation (email format, password length, image URL, price > 0,
  rating 1–5, required fields).

**Technologies.** Java 17, Servlets (Jakarta EE 10), JSP, JDBC, MySQL, Redis
(via Jedis), Gson, JJWT, SLF4J, Maven, JUnit 5 (declared, no tests written).

**How it matches the university requirements.**

| Requirement | Where it lives |
|---|---|
| MVC layering | `controller`, `service`, `dao`, `model` packages |
| Servlets | `com.ecommerce.controller.*` |
| Helper / Utility | `com.ecommerce.helper.*`, `com.ecommerce.util.*` |
| POJO / Model | `com.ecommerce.model.*` |
| Filters | `com.ecommerce.filter.*` |
| Auth filter | [AuthFilter.java](src/main/java/com/ecommerce/filter/AuthFilter.java) |
| MySQL + DAOs | [schema.sql](src/main/resources/schema.sql) + `dao.impl.*` |
| Redis cache | [ProductService.java](src/main/java/com/ecommerce/service/ProductService.java) |
| Sign up / Sign in / Logout | [AuthServlet.java](src/main/java/com/ecommerce/controller/AuthServlet.java) |
| Session + JWT | [AuthServlet.java](src/main/java/com/ecommerce/controller/AuthServlet.java) + [AuthFilter.java](src/main/java/com/ecommerce/filter/AuthFilter.java) |
| Add / delete product | [ProductServlet.java](src/main/java/com/ecommerce/controller/ProductServlet.java) |
| Rate limiting | [RateLimitFilter.java](src/main/java/com/ecommerce/filter/RateLimitFilter.java) |
| Error handling | [ErrorHandlingFilter.java](src/main/java/com/ecommerce/filter/ErrorHandlingFilter.java) + error pages |
| Admin privileges | [AdminFilter.java](src/main/java/com/ecommerce/filter/AdminFilter.java) |
| Product details page | [product-details.jsp](src/main/webapp/product-details.jsp) |

---

## 2. Required Features Checklist

| # | Requirement | Implemented? | Where in code | Explanation |
|---|---|---|---|---|
| 1 | Home page shows all products | Yes | [ProductServlet.java](src/main/java/com/ecommerce/controller/ProductServlet.java), [index.jsp](src/main/webapp/index.jsp) | `/` forwards to `index.jsp` with `productsWithReviews`. |
| 2 | Logged-in user info shown | Yes | [navbar.jsp](src/main/webapp/navbar.jsp) | Name, email and admin badge. |
| 3 | Review / feedback section | Yes | [index.jsp](src/main/webapp/index.jsp), [product-details.jsp](src/main/webapp/product-details.jsp) | Each product card and the details page list reviews. |
| 4 | Servlets | Yes | `controller/*.java` | 5 servlets total. |
| 5 | Helper classes | Yes | `helper/*.java`, `util/*.java` | DBConnection, JwtHelper, PasswordHelper, RedisHelper, JsonUtil, ValidationUtil. |
| 6 | POJO / Model classes | Yes | `model/User.java`, `model/Product.java`, `model/Review.java` | Plain data holders. |
| 7 | Filters | Yes | `filter/*.java` | 4 filters: RateLimit, ErrorHandling, Auth, Admin. |
| 8 | AuthFilter | Yes | [AuthFilter.java](src/main/java/com/ecommerce/filter/AuthFilter.java) | Authenticates via Bearer / JWT cookie / session. |
| 9 | MySQL DB | Yes | [DBConnection.java](src/main/java/com/ecommerce/helper/DBConnection.java) | JDBC + MySQL connector. |
| 10 | users / products / reviews tables | Yes | [schema.sql](src/main/resources/schema.sql) | 3 tables with FKs. |
| 11 | Redis product caching | Yes | [ProductService.java](src/main/java/com/ecommerce/service/ProductService.java) | Key `products:all`, TTL 300 s. |
| 12 | Redis rate limiting | Yes | [RateLimitFilter.java](src/main/java/com/ecommerce/filter/RateLimitFilter.java) | 30 req / 60 s / IP. |
| 13 | Sign up | Yes | [AuthServlet.java](src/main/java/com/ecommerce/controller/AuthServlet.java) `/signup` | – |
| 14 | Sign in | Yes | [AuthServlet.java](src/main/java/com/ecommerce/controller/AuthServlet.java) `/signin` | – |
| 15 | Session auth | Yes | [AuthServlet.java](src/main/java/com/ecommerce/controller/AuthServlet.java) → `session.setAttribute("user", …)` | Default browser flow. |
| 16 | JWT auth | Yes | [JwtHelper.java](src/main/java/com/ecommerce/helper/JwtHelper.java), [AuthFilter.java](src/main/java/com/ecommerce/filter/AuthFilter.java) | Bearer header **or** `jwt` HttpOnly cookie. |
| 17 | JWT creation + validation | Yes | [JwtHelper.java](src/main/java/com/ecommerce/helper/JwtHelper.java) | HS256, 12 h. |
| 18 | Logout | Yes | [AuthServlet.java](src/main/java/com/ecommerce/controller/AuthServlet.java) `/logout` | Blacklists JWT + clears cookie + invalidates session. |
| 19 | Delete account | Yes | [ProfileServlet.java](src/main/java/com/ecommerce/controller/ProfileServlet.java) `/profile/delete` | – |
| 20 | Add product | Yes | [ProductServlet.java](src/main/java/com/ecommerce/controller/ProductServlet.java) `/products/add` | Admin-only. |
| 21 | Delete product | Yes | [ProductServlet.java](src/main/java/com/ecommerce/controller/ProductServlet.java) `/products/delete` | Admin-only. |
| 22 | Edit product (bonus) | Yes | [ProductServlet.java](src/main/java/com/ecommerce/controller/ProductServlet.java) `/products/edit` | Admin-only. |
| 23 | Error handling | Yes | [ErrorHandlingFilter.java](src/main/java/com/ecommerce/filter/ErrorHandlingFilter.java) + 400/403/404/500 JSP | Centralized in a filter. |
| 24 | Clean code, layers | Yes | controller/service/dao/model/filter/util/helper/exception | – |
| 25 | Product details page | Yes | [product-details.jsp](src/main/webapp/product-details.jsp) | – |
| 26 | Admin privileges | Yes | [AdminFilter.java](src/main/java/com/ecommerce/filter/AdminFilter.java) | Hard 403 for non-admins. |
| 27 | Layer separation | Yes | The whole `com.ecommerce.*` tree | – |
| 28 | Server-side validation | Yes | [ValidationUtil.java](src/main/java/com/ecommerce/util/ValidationUtil.java) | Used by Auth/Product/Review services. |

---

## 3. Technologies Used

For each tech: *what it is → why it's here → which files use it → a tiny example.*

### Java 17
- The language. We're on Java 17 (see `<maven.compiler.source>17</maven.compiler.source>` in [pom.xml](pom.xml)).
- Uses modern features like text blocks (in [ReviewDAOImpl.java](src/main/java/com/ecommerce/dao/impl/ReviewDAOImpl.java)) and pattern matching (`instanceof String s` in [AuthServlet.java](src/main/java/com/ecommerce/controller/AuthServlet.java)).

### Maven
- Build tool. Reads [pom.xml](pom.xml), downloads dependencies, compiles, packages as a WAR.
- One command: `./mvnw clean package` produces `target/eCommerce-1.0-SNAPSHOT.war`.

### Servlet (Jakarta EE 10)
- A **Servlet** is a Java class that handles HTTP requests. Each servlet maps to one or more URLs.
- We declare URL mappings with `@WebServlet(urlPatterns = {…})` annotations.
- Example: [ProductServlet.java](src/main/java/com/ecommerce/controller/ProductServlet.java) handles `/`, `/products`, `/products/details`, `/products/add`, etc.
- Two main methods: `doGet` and `doPost`.

### JSP (JavaServer Pages)
- A view template that mixes HTML with Java scriptlets `<% ... %>`. Compiled into a servlet at runtime.
- Each JSP receives data via `request.setAttribute(...)` from a servlet and renders HTML.
- Example: [index.jsp](src/main/webapp/index.jsp) reads `productsWithReviews` and loops over it.

### JDBC (Java Database Connectivity)
- The Java API for talking to a SQL database.
- We use `Connection`, `PreparedStatement`, `ResultSet` throughout `dao.impl.*`.
- See [DBConnection.java](src/main/java/com/ecommerce/helper/DBConnection.java).

### MySQL
- The relational database we store users / products / reviews in.
- Driver: `com.mysql:mysql-connector-j:9.3.0` (in [pom.xml](pom.xml)).
- Schema: [schema.sql](src/main/resources/schema.sql).

### Redis
- An in-memory key-value store used for:
  - product list caching (`products:all`),
  - per-IP rate limiting (`ratelimit:<ip>`),
  - JWT blacklist (`jwt:blacklist:<token>`).

### Jedis
- Java client for Redis. Dependency `redis.clients:jedis:6.2.0`.
- We use a `JedisPool` in [RedisHelper.java](src/main/java/com/ecommerce/helper/RedisHelper.java).

### JWT (JSON Web Token)
- A small, signed token containing user identity. Generated at login, validated on every protected request.
- Library: `io.jsonwebtoken:jjwt-*:0.12.6`.
- Helpers: [JwtHelper.java](src/main/java/com/ecommerce/helper/JwtHelper.java).
- Algorithm: HS256 (HMAC-SHA256). Expiry: 12 hours.

### Cookie
- A small piece of data the server tells the browser to remember and send back on each request.
- We use one cookie: `jwt`, set as `HttpOnly` so JavaScript cannot read it.
- See [AuthServlet.java](src/main/java/com/ecommerce/controller/AuthServlet.java) (`writeJwtCookie`).

### Session (HttpSession)
- Server-side memory associated with a browser via the `JSESSIONID` cookie.
- We store the `user` object and a `jwt` string in the session after login.
- See [AuthServlet.java](src/main/java/com/ecommerce/controller/AuthServlet.java) and [navbar.jsp](src/main/webapp/navbar.jsp).

### Filter
- A class that intercepts requests **before** they reach a servlet. Filters can check auth, rate-limit, catch errors, etc.
- We have four filters in [src/main/java/com/ecommerce/filter/](src/main/java/com/ecommerce/filter/). Order is fixed in [web.xml](src/main/webapp/WEB-INF/web.xml).

### DAO (Data Access Object)
- A class that hides SQL behind plain Java methods.
- Example: instead of writing `SELECT * FROM products` in the servlet, the servlet calls `productDAO.findAll()`.
- Files: [ProductDAOImpl.java](src/main/java/com/ecommerce/dao/impl/ProductDAOImpl.java), [UserDAOImpl.java](src/main/java/com/ecommerce/dao/impl/UserDAOImpl.java), [ReviewDAOImpl.java](src/main/java/com/ecommerce/dao/impl/ReviewDAOImpl.java).

### Service layer
- Holds **business rules**: validation, password hashing, cache invalidation, etc. Lives between the servlet and the DAO.
- Example: [AuthService.java](src/main/java/com/ecommerce/service/AuthService.java) checks email uniqueness before calling `userDAO.create`.

### POJO / Model
- A "Plain Old Java Object" — just fields + getters/setters.
- Files: [User.java](src/main/java/com/ecommerce/model/User.java), [Product.java](src/main/java/com/ecommerce/model/Product.java), [Review.java](src/main/java/com/ecommerce/model/Review.java).

### web.xml
- The deployment descriptor. Configures filter order, welcome file, and error pages.
- File: [src/main/webapp/WEB-INF/web.xml](src/main/webapp/WEB-INF/web.xml).

---

## 4. Project Architecture

Every HTTP request travels through the following pipeline.

```
                          ┌─────────────────────────┐
                          │        Browser          │
                          └──────────┬──────────────┘
                                     │  HTTP request
                                     ▼
   ┌─────────────────────────────────────────────────────────┐
   │  Tomcat servlet container (deployed WAR)                │
   │                                                         │
   │  1) RateLimitFilter        — Redis INCR per IP          │
   │  2) ErrorHandlingFilter    — try/catch around the chain │
   │  3) AuthFilter             — JWT (header/cookie) or     │
   │                              session check              │
   │  4) AdminFilter            — role == ADMIN check        │
   │                                                         │
   │  5) Servlet (Controller)   — AuthServlet,               │
   │                              ProductServlet, …          │
   │  6) Service                — AuthService, …             │
   │  7) DAO                    — UserDAOImpl, …             │
   │  8) DBConnection           — JDBC DriverManager         │
   │                                                         │
   │  9) JSP                    — render HTML                │
   └─────────────────────────────────────────────────────────┘
              │                                  │
              ▼                                  ▼
        ┌──────────┐                       ┌──────────┐
        │  MySQL   │                       │  Redis   │
        │  users   │                       │  cache,  │
        │ products │                       │ rate-lim │
        │ reviews  │                       │ blacklist│
        └──────────┘                       └──────────┘
```

Redis serves three jobs:
1. **Product cache** — `products:all` (TTL 300 s) in [ProductService.java](src/main/java/com/ecommerce/service/ProductService.java).
2. **Rate limiting** — `ratelimit:<ip>` (TTL 60 s) in [RateLimitFilter.java](src/main/java/com/ecommerce/filter/RateLimitFilter.java).
3. **JWT blacklist** — `jwt:blacklist:<token>` in [AuthFilter.java](src/main/java/com/ecommerce/filter/AuthFilter.java) and [AuthServlet.java](src/main/java/com/ecommerce/controller/AuthServlet.java).

---

## 5. Folder and Package Structure

```
src/main/
├── java/com/ecommerce/
│   ├── controller/       (Servlets — entry points for URLs)
│   ├── service/          (Business logic)
│   ├── dao/              (DAO interfaces)
│   ├── dao/impl/         (DAO JDBC implementations)
│   ├── model/            (POJOs: User, Product, Review)
│   ├── filter/           (RateLimit, ErrorHandling, Auth, Admin)
│   ├── helper/           (DBConnection, JwtHelper, PasswordHelper, RedisHelper)
│   ├── util/             (JsonUtil, ValidationUtil)
│   └── exception/        (Custom RuntimeException subclasses)
├── resources/
│   └── schema.sql        (Database tables)
└── webapp/
    ├── *.jsp             (View templates)
    ├── css/, js/
    └── WEB-INF/web.xml   (Filter order + error pages)
```

### `controller` (Servlets)
Each servlet is a small router for a group of URLs.
- [AuthServlet.java](src/main/java/com/ecommerce/controller/AuthServlet.java) — `/signin`, `/signup`, `/logout`
- [ProductServlet.java](src/main/java/com/ecommerce/controller/ProductServlet.java) — `/`, `/products`, `/products/details`, `/products/add`, `/products/edit`, `/products/delete`
- [ReviewServlet.java](src/main/java/com/ecommerce/controller/ReviewServlet.java) — `/reviews/add`, `/reviews/delete` (POST-only)
- [AdminServlet.java](src/main/java/com/ecommerce/controller/AdminServlet.java) — `/admin`
- [ProfileServlet.java](src/main/java/com/ecommerce/controller/ProfileServlet.java) — `/profile`, `/profile/delete`

### `service`
Business rules.
- [AuthService.java](src/main/java/com/ecommerce/service/AuthService.java) — register / login
- [UserService.java](src/main/java/com/ecommerce/service/UserService.java) — find / delete user
- [ProductService.java](src/main/java/com/ecommerce/service/ProductService.java) — CRUD + cache
- [ReviewService.java](src/main/java/com/ecommerce/service/ReviewService.java) — reviews

### `dao` and `dao.impl`
Interface + implementation pattern.
- [UserDAO.java](src/main/java/com/ecommerce/dao/UserDAO.java) → [UserDAOImpl.java](src/main/java/com/ecommerce/dao/impl/UserDAOImpl.java)
- [ProductDAO.java](src/main/java/com/ecommerce/dao/ProductDAO.java) → [ProductDAOImpl.java](src/main/java/com/ecommerce/dao/impl/ProductDAOImpl.java)
- [ReviewDAO.java](src/main/java/com/ecommerce/dao/ReviewDAO.java) → [ReviewDAOImpl.java](src/main/java/com/ecommerce/dao/impl/ReviewDAOImpl.java)

The interface lets us swap implementations later — for now there's one.

### `model`
- [User.java](src/main/java/com/ecommerce/model/User.java): `id, name, email, passwordHash, role`
- [Product.java](src/main/java/com/ecommerce/model/Product.java): `id, name, description, price (BigDecimal), imageUrl`
- [Review.java](src/main/java/com/ecommerce/model/Review.java): `id, productId, userId, reviewerName, rating, comment`

### `filter`
- [RateLimitFilter.java](src/main/java/com/ecommerce/filter/RateLimitFilter.java) — runs on `/*`
- [ErrorHandlingFilter.java](src/main/java/com/ecommerce/filter/ErrorHandlingFilter.java) — runs on `/*`
- [AuthFilter.java](src/main/java/com/ecommerce/filter/AuthFilter.java) — protects `/profile`, `/profile/*`, `/reviews/*`, `/products/add|edit|delete`, `/admin`
- [AdminFilter.java](src/main/java/com/ecommerce/filter/AdminFilter.java) — protects `/products/add|edit|delete`, `/admin`

### `helper`
- [DBConnection.java](src/main/java/com/ecommerce/helper/DBConnection.java) — plain `DriverManager`-based connection
- [JwtHelper.java](src/main/java/com/ecommerce/helper/JwtHelper.java) — generate / validate tokens
- [PasswordHelper.java](src/main/java/com/ecommerce/helper/PasswordHelper.java) — PBKDF2 hashing
- [RedisHelper.java](src/main/java/com/ecommerce/helper/RedisHelper.java) — Jedis pool wrapper

### `util`
- [JsonUtil.java](src/main/java/com/ecommerce/util/JsonUtil.java) — Gson + `writeJson(resp, status, payload)`
- [ValidationUtil.java](src/main/java/com/ecommerce/util/ValidationUtil.java) — all server-side validators

### `exception`
- [ValidationException.java](src/main/java/com/ecommerce/exception/ValidationException.java)
- [UnauthorizedException.java](src/main/java/com/ecommerce/exception/UnauthorizedException.java)
- [UserNotFoundException.java](src/main/java/com/ecommerce/exception/UserNotFoundException.java)

### `webapp`
JSPs and the deployment descriptor.

---

## 6. Database Design

File: [src/main/resources/schema.sql](src/main/resources/schema.sql)

### Tables

**users**
| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT PK auto-increment | – |
| `name` | VARCHAR(120) NOT NULL | – |
| `email` | VARCHAR(180) NOT NULL UNIQUE | login id |
| `password_hash` | VARCHAR(255) NOT NULL | PBKDF2 output |
| `role` | VARCHAR(20) NOT NULL DEFAULT 'USER' | `'USER'` or `'ADMIN'` |
| `created_at` | TIMESTAMP DEFAULT CURRENT_TIMESTAMP | – |

**products**
| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT PK auto-increment | – |
| `name` | VARCHAR(180) NOT NULL | – |
| `description` | TEXT NOT NULL | – |
| `price` | DECIMAL(10,2) NOT NULL CHECK (price > 0) | – |
| `image_url` | VARCHAR(500) NOT NULL | – |
| `created_at` | TIMESTAMP DEFAULT CURRENT_TIMESTAMP | – |

**reviews**
| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT PK auto-increment | – |
| `product_id` | BIGINT NOT NULL → `products(id)` ON DELETE CASCADE | – |
| `user_id` | BIGINT NOT NULL → `users(id)` ON DELETE CASCADE | – |
| `rating` | INT NOT NULL CHECK (1–5) | – |
| `comment` | TEXT NOT NULL | – |
| `created_at` | TIMESTAMP DEFAULT CURRENT_TIMESTAMP | – |

### Relationships in plain English
- **A user has many reviews.** One row in `users` can be referenced by many `reviews.user_id`.
- **A product has many reviews.** One row in `products` can be referenced by many `reviews.product_id`.
- **A review belongs to exactly one product and one user.**
- **ON DELETE CASCADE** — when a user or product is deleted, MySQL automatically deletes all of their reviews. We rely on this in delete-account and delete-product flows.

### Why a relational DB?
The data is highly structured and we need consistency (e.g., a review's `user_id` must point to a real user). Relational integrity (foreign keys) gives us that guarantee for free.

### DAOs & SQL Injection prevention
Every SQL statement in the codebase uses `PreparedStatement` with `?` placeholders, never string concatenation. The driver escapes parameters for us. Example:

```java
// from UserDAOImpl.findByEmail
String sql = "SELECT id, name, email, password_hash, role FROM users WHERE email = ?";
try (Connection connection = DBConnection.getConnection();
     PreparedStatement statement = connection.prepareStatement(sql)) {
    statement.setString(1, email);   // safe: cannot break out of the placeholder
    ...
}
```

If we had instead built the SQL with `"… WHERE email = '" + email + "'"`, a malicious email like `' OR '1'='1` would have logged the attacker in. PreparedStatement prevents that.

---

## 7. Request Lifecycle

### Vocabulary (read this first)

- **Request** — what the browser sends to the server (method like GET/POST, URL, headers, body).
- **Response** — what the server sends back (status code, headers, body).
- **Servlet** — a Java class with `doGet` / `doPost` methods that receives requests and produces responses.
- **Filter** — a class that runs *before* the servlet. It can stop the request, decorate it, or let it through.
- **Forward** — same request continues to a different resource on the server (`request.getRequestDispatcher("/x.jsp").forward(req, resp)`). The browser URL doesn't change.
- **Redirect** — server tells the browser "go fetch this other URL" (`response.sendRedirect("/x")`). The browser does a new request.
- **Request attribute** — a value stored on `request`, alive for one request only (`req.setAttribute("user", user)`).
- **Session attribute** — a value stored on `HttpSession`, alive for many requests from the same browser (`req.getSession().setAttribute("user", user)`).
- **Cookie** — a small text item the server tells the browser to remember and send back. We use `jwt` (HttpOnly).

### General flow

```
Browser  ─POST /signin─►  RateLimitFilter
                         │   (Redis INCR ratelimit:<ip>; 429 if too many)
                         ▼
                         ErrorHandlingFilter
                         │   (try { chain.doFilter(...) } catch (...) → 4xx/5xx)
                         ▼
                         AuthFilter        (does NOT match /signin, skipped)
                         ▼
                         AdminFilter       (does NOT match /signin, skipped)
                         ▼
                         AuthServlet.doPost
                         │
                         ▼
                         AuthService.login
                         │   - ValidationUtil.validateEmail
                         │   - userDAO.findByEmail
                         │   - PasswordHelper.verifyPassword
                         │   - JwtHelper.generateToken
                         ▼
                         (back in servlet) write session + jwt cookie
                         ▼
Browser ◄─302 /─         response.sendRedirect("/")
```

---

## 8. Authentication System

### Sign Up flow
1. User submits POST `/signup` from [register.jsp](src/main/webapp/register.jsp).
2. [AuthServlet.java](src/main/java/com/ecommerce/controller/AuthServlet.java) calls [AuthService.register](src/main/java/com/ecommerce/service/AuthService.java).
3. `AuthService.register` validates the input via [ValidationUtil.java](src/main/java/com/ecommerce/util/ValidationUtil.java), checks that the email isn't already used (`userDAO.findByEmail(email)`), hashes the password with [PasswordHelper.hashPassword](src/main/java/com/ecommerce/helper/PasswordHelper.java), and inserts the user with role `'USER'`.
4. The servlet sets `user` and `jwt` in session, sets the `jwt` HttpOnly cookie, and redirects to `/`.

### Sign In flow
1. POST `/signin` → AuthServlet → AuthService.login.
2. `findByEmail` returns the user; `PasswordHelper.verifyPassword` checks the PBKDF2 hash.
3. On success: generate JWT, return `{user, token}` to the servlet.
4. Servlet stores both in session, writes the `jwt` cookie, redirects to `/`.

### Password hashing
[PasswordHelper.java](src/main/java/com/ecommerce/helper/PasswordHelper.java) uses **PBKDF2-HmacSHA256** with:
- Random 16-byte salt per user
- 65 536 iterations
- 256-bit key length
- Stored as `base64(salt) + ":" + base64(hash)`

We never store the plain password. Even if the DB leaks, attackers cannot recover passwords easily.

### Session-based authentication
After login, `session.setAttribute("user", user)` stores the user server-side. The browser carries a `JSESSIONID` cookie automatically. Used by the JSP UI (navbar, profile page, review owner check).

### JWT-based authentication
[JwtHelper.java](src/main/java/com/ecommerce/helper/JwtHelper.java):
- `generateToken(userId, role)` — builds an HS256 JWT with the user's id as subject and the role as a claim. Expires in 12 hours.
- `validateToken(token)` — verifies the signature and expiry.
- `getUserIdFromToken`, `getRoleFromToken` — extract claims.
- The signing key is read from env var `ECOMMERCE_JWT_SECRET` (default placeholder).

### JWT cookie support
On login/signup, we set a cookie called `jwt`:
- `HttpOnly` (JS cannot read it — protects against XSS theft)
- `Path = <context-path>`
- `Max-Age = 43 200` (12 hours)
- `Secure` when the request is HTTPS

The browser sends this cookie back on every request automatically. [AuthFilter.java](src/main/java/com/ecommerce/filter/AuthFilter.java) reads it via `extractTokenFromCookie`.

### AuthFilter behavior
On a protected URL, AuthFilter checks (in order):
1. `Authorization: Bearer <token>` header → validate JWT
2. `jwt` cookie → validate JWT
3. HTTP session → look for `user` attribute
4. Otherwise: redirect to `/login.jsp` (or 401 JSON if `Accept: application/json`)

It also short-circuits to 401 if the token is in the **JWT blacklist** (`jwt:blacklist:<token>` in Redis).

### Logout flow
[AuthServlet.java](src/main/java/com/ecommerce/controller/AuthServlet.java) at POST `/logout`:
1. Looks for the JWT in session → cookie → Authorization header.
2. If found, calls `AuthFilter.blacklistToken(token)` which writes the token to Redis with the remaining TTL.
3. Sends a `jwt` cookie with `Max-Age=0` to delete it in the browser.
4. Calls `session.invalidate()` to drop the server-side session.

### Session vs JWT
| Aspect | Session | JWT |
|---|---|---|
| Where stored | Server memory + JSESSIONID cookie | Inside the token itself (signed) |
| State | Stateful | Stateless |
| Scaling | Sticky sessions or shared store | Easier to scale horizontally |
| Revocation | Easy (`session.invalidate()`) | Harder — needs a blacklist |
| Used in this project | Default browser path | Header (API), `jwt` cookie (browser), session attribute (legacy) |

### 401 vs 403
- **401 Unauthorized** = "I don't know who you are" — login first.
- **403 Forbidden** = "I know who you are but you don't have permission" — used by `AdminFilter`.

---

## 9. Authorization / Admin System

### What is authorization?
Authentication answers *"who are you?"*; authorization answers *"are you allowed to do this?"*. After AuthFilter sets `req.setAttribute("user", user)`, AdminFilter answers the second question for admin URLs.

### Roles
- **`USER`** (default) — can browse products, write & delete *their own* reviews, view & delete their account.
- **`ADMIN`** — can do everything a USER can, plus add / edit / delete products and view the admin dashboard.

The role lives in `users.role` (default `'USER'`). To make a user admin, run:
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'someone@example.com';
```

### How AdminFilter works
[AdminFilter.java](src/main/java/com/ecommerce/filter/AdminFilter.java) maps to `/products/add`, `/products/edit`, `/products/delete`, `/admin`. It reads the `user` from `req.getAttribute("user")` (set by AuthFilter) or falls back to the session, then checks `user.getRole().equalsIgnoreCase("ADMIN")`. If not admin → `sendError(403)` which renders [403.jsp](src/main/webapp/403.jsp).

### Why ProductServlet doesn't check admin itself
Because by the time a request reaches `ProductServlet.doPost("/products/add")`, both AuthFilter and AdminFilter have already approved it. The servlet can focus on the actual work — separation of concerns.

### Scenarios
- **Unauthenticated user** opens `/admin` → AuthFilter redirects to `/login.jsp` (the request never reaches AdminFilter).
- **Normal USER** opens `/admin` → AuthFilter lets them through (they're logged in), AdminFilter sees role != ADMIN → 403.
- **ADMIN** opens `/admin` → both filters pass → [AdminServlet](src/main/java/com/ecommerce/controller/AdminServlet.java) forwards to [admin-dashboard.jsp](src/main/webapp/admin-dashboard.jsp).

---

## 10. Product Management

### View all products
- URL: `/` (also `/products`)
- Servlet: [ProductServlet.java](src/main/java/com/ecommerce/controller/ProductServlet.java) `doGet`
- Service: [ProductService.getProductsWithReviews](src/main/java/com/ecommerce/service/ProductService.java)
  - Calls `getAllProducts()` (Redis cache or DB)
  - For each product, fetches its reviews via `reviewService.getByProduct(productId)` — **simple loop**
- JSP: [index.jsp](src/main/webapp/index.jsp)

### View product details
- URL: `/products/details?id=<n>`
- Servlet uses a safe `parseId` helper — invalid id returns 400 (not 500).
- Service: `productService.getById(id)` returns `Optional<Product>`. Missing → 404.
- Reviews fetched with `reviewService.getByProduct(id)`.
- JSP: [product-details.jsp](src/main/webapp/product-details.jsp).

### Add product (admin only)
- URL: `POST /products/add`
- Filters: RateLimit → ErrorHandling → AuthFilter → AdminFilter.
- Service: `productService.addProduct(name, description, price, imageUrl)`
  - Validates each field via [ValidationUtil](src/main/java/com/ecommerce/util/ValidationUtil.java).
  - Inserts into MySQL via [ProductDAOImpl.create](src/main/java/com/ecommerce/dao/impl/ProductDAOImpl.java).
  - **Invalidates Redis cache** (`RedisHelper.del("products:all")`).
- Redirect to `/admin`.

### Edit product (admin only)
- URL: `POST /products/edit`
- Same filter chain. `productService.updateProduct(...)`. Cache invalidated. Redirect to `/admin`.

### Delete product (admin only)
- URL: `POST /products/delete`
- Same chain. `productDAO.deleteById(id)` — FK `ON DELETE CASCADE` removes the product's reviews automatically.

### Validation
[ValidationUtil.java](src/main/java/com/ecommerce/util/ValidationUtil.java) enforces:
- Product name + description: non-blank
- Image URL: starts with `http://` or `https://`
- Price: a positive `BigDecimal`

If anything fails, a `ValidationException` is thrown — caught by [ErrorHandlingFilter](src/main/java/com/ecommerce/filter/ErrorHandlingFilter.java) which returns 400.

### Cache invalidation
After every write that affects the product list (add/edit/delete) we delete the `products:all` Redis key. The next read sees a cache miss and rebuilds it. This keeps cache and DB consistent.

### Files involved
- [ProductServlet.java](src/main/java/com/ecommerce/controller/ProductServlet.java)
- [ProductService.java](src/main/java/com/ecommerce/service/ProductService.java)
- [ProductDAO.java](src/main/java/com/ecommerce/dao/ProductDAO.java) + [ProductDAOImpl.java](src/main/java/com/ecommerce/dao/impl/ProductDAOImpl.java)
- [index.jsp](src/main/webapp/index.jsp), [product-details.jsp](src/main/webapp/product-details.jsp), [admin-dashboard.jsp](src/main/webapp/admin-dashboard.jsp)

---

## 11. Review / Feedback System

### Display reviews
- On the **home page** every product card shows its reviews ([index.jsp](src/main/webapp/index.jsp)).
- On the **product details page** every review is listed with the rating and comment ([product-details.jsp](src/main/webapp/product-details.jsp)).
- The data flows from [ReviewDAOImpl.findByProductId](src/main/java/com/ecommerce/dao/impl/ReviewDAOImpl.java) which joins `reviews` with `users` to get the `reviewer_name`.

### Add a review
- URL: `POST /reviews/add`
- AuthFilter requires a logged-in user.
- ReviewServlet checks that the role is `USER` (admins cannot leave reviews — a deliberate design choice).
- `ReviewService.addReview` validates the rating (1–5) and the comment, then inserts a row.

### Delete a review
- URL: `POST /reviews/delete` (changed from GET).
- The JSP sends a tiny `<form method="post">` with a hidden `reviewId`.
- `ReviewServlet.doPost("/reviews/delete")` checks the user is logged in, parses the id safely (`Long.parseLong` in a try/catch — returns 400 on bad input).
- `ReviewService.deleteReview(reviewId, userId)` calls the DAO method `deleteById(reviewId, userId)`.

### Ownership check (important!)
The DAO query is:
```sql
DELETE FROM reviews WHERE id = ? AND user_id = ?
```
It refuses to delete if the row doesn't belong to the calling user. If 0 rows are affected, a `RuntimeException` is thrown. **A user cannot delete someone else's review** — the safety check happens in the SQL, not in Java.

### Why POST instead of GET
1. **HTTP semantics** — GET should be safe (idempotent, no state change). A delete changes state.
2. **CSRF surface** — GET requests can be triggered by `<img src="…">` in an attacker's page. POST forms require a same-origin form submit. (Real CSRF protection would also need a synchronizer token — a future improvement.)
3. **Caches & prefetchers** — a search engine prefetcher could fetch a delete-URL and wipe data.

### Files involved
- [ReviewServlet.java](src/main/java/com/ecommerce/controller/ReviewServlet.java)
- [ReviewService.java](src/main/java/com/ecommerce/service/ReviewService.java)
- [ReviewDAO.java](src/main/java/com/ecommerce/dao/ReviewDAO.java) + [ReviewDAOImpl.java](src/main/java/com/ecommerce/dao/impl/ReviewDAOImpl.java)
- [index.jsp](src/main/webapp/index.jsp), [product-details.jsp](src/main/webapp/product-details.jsp)

### Loading reviews on the home page — current style
[ProductService.getProductsWithReviews](src/main/java/com/ecommerce/service/ProductService.java) uses a **simple loop**:

```java
public Map<Product, List<Review>> getProductsWithReviews() {
    List<Product> products = getAllProducts();
    Map<Product, List<Review>> productsWithReviews = new LinkedHashMap<>();
    for (Product product : products) {
        List<Review> reviews = reviewService.getByProduct(product.getId());
        productsWithReviews.put(product, reviews);
    }
    return productsWithReviews;
}
```

This is intentionally simple and easy to read for a university-level project.

In bigger systems this is called the **"N+1 query" pattern** — 1 query for products plus N queries (one per product) for reviews. For our data sizes that's fine, but be ready to mention it as a known limitation in the discussion.

---

## 12. Redis Usage

### What Redis is
Redis is an in-memory key-value store. Because data lives in RAM, reads and writes are very fast (sub-millisecond). It's perfect for caches, counters, and short-lived data.

### Connection
[RedisHelper.java](src/main/java/com/ecommerce/helper/RedisHelper.java) opens a single `JedisPool` on class load. Host and port come from env vars (`ECOMMERCE_REDIS_HOST`, `ECOMMERCE_REDIS_PORT` — default `localhost:6380`).

### 1. Product caching
- Key: `products:all`
- TTL: 300 seconds
- Operation: `setex(key, 300, json)` to set; `get(key)` to read; `del(key)` to invalidate.
- Code: [ProductService.getAllProducts](src/main/java/com/ecommerce/service/ProductService.java).

**Cache hit:** the JSON is returned and we skip the DB.
**Cache miss / Redis down:** we fall back to `productDAO.findAll()` and (best-effort) write the result back to Redis.

After `addProduct`, `updateProduct`, or `deleteProduct` we call `RedisHelper.del("products:all")` so the next read repopulates the cache.

### 2. Rate limiting
[RateLimitFilter.java](src/main/java/com/ecommerce/filter/RateLimitFilter.java) runs on every request.
- Key: `ratelimit:<client-ip>`
- Operation: `INCR` the counter on every request; `EXPIRE 60` the first time we see this IP in a window.
- Threshold: > 30 in 60 seconds → respond `429 Too Many Requests` JSON.
- If Redis is down: the filter logs and **lets the request through** (fail-open).

### 3. JWT blacklist
- Key: `jwt:blacklist:<token>`
- Written by `AuthFilter.blacklistToken` on logout, with TTL = remaining lifetime of the token.
- Read by `AuthFilter.isTokenBlacklisted` on every JWT request → if present, 401.
- **Limitation** — if Redis is down at the moment of logout, the blacklist write is silently dropped. The session is still invalidated and the cookie is still cleared, so the browser is logged out, but a stolen token could in theory keep working until its 12-hour expiry.

### Files using Redis
- [RedisHelper.java](src/main/java/com/ecommerce/helper/RedisHelper.java)
- [RateLimitFilter.java](src/main/java/com/ecommerce/filter/RateLimitFilter.java)
- [ProductService.java](src/main/java/com/ecommerce/service/ProductService.java)
- [AuthFilter.java](src/main/java/com/ecommerce/filter/AuthFilter.java) (blacklist read + write)
- [AuthServlet.java](src/main/java/com/ecommerce/controller/AuthServlet.java) (logout calls blacklist)

---

## 13. Error Handling

### `ErrorHandlingFilter`
[ErrorHandlingFilter.java](src/main/java/com/ecommerce/filter/ErrorHandlingFilter.java) wraps everything below it in one `try/catch`:

| Caught | Becomes |
|---|---|
| `ValidationException` | 400 Bad Request |
| `NumberFormatException` | 400 Bad Request |
| `UnauthorizedException` | 401 (or redirect to `/login.jsp` for HTML) |
| `UserNotFoundException` | 404 Not Found |
| Any other `Exception` | 500 Internal Server Error (logged, stack trace hidden) |

The filter never leaks SQL details or Java stack traces to the user — only a generic message.

### Error pages (mapped in [web.xml](src/main/webapp/WEB-INF/web.xml))
- [400.jsp](src/main/webapp/400.jsp) — "Bad Request"
- [403.jsp](src/main/webapp/403.jsp) — "Forbidden"
- [404.jsp](src/main/webapp/404.jsp) — "Not Found"
- [500.jsp](src/main/webapp/500.jsp) — "Internal Server Error"

Each page is plain HTML — no SQL, no stack traces.

### Safe ID parsing
[ProductServlet.java](src/main/java/com/ecommerce/controller/ProductServlet.java) defines a private helper:
```java
private static Optional<Long> parseId(String raw) {
    if (raw == null || raw.isBlank()) return Optional.empty();
    try {
        long id = Long.parseLong(raw.trim());
        if (id <= 0) return Optional.empty();
        return Optional.of(id);
    } catch (NumberFormatException ex) {
        return Optional.empty();
    }
}
```
A request to `/products/details?id=abc` now returns a clean **400** instead of a Java stack trace.

### HTTP status codes used
| Code | Meaning | When |
|---|---|---|
| 400 Bad Request | Client sent invalid input | Bad id, validation failure |
| 401 Unauthorized | Not logged in | AuthFilter / blacklisted JWT |
| 403 Forbidden | Logged in but not allowed | AdminFilter rejects USER |
| 404 Not Found | Resource doesn't exist | Product id not in DB, unknown URL |
| 429 Too Many Requests | Rate limit hit | RateLimitFilter |
| 500 Internal Server Error | Bug, DB failure, etc. | Anything uncaught |

---

## 14. Security Concepts in the Project

| Concept | Where | How |
|---|---|---|
| Password hashing | [PasswordHelper.java](src/main/java/com/ecommerce/helper/PasswordHelper.java) | PBKDF2-HmacSHA256, salted, 65 536 iterations |
| PreparedStatement everywhere | all DAO impls | Parameter binding — no string concat in SQL |
| SQL injection prevention | DAOs | A consequence of using PreparedStatement |
| Session protection | [AuthServlet logout](src/main/java/com/ecommerce/controller/AuthServlet.java) | `session.invalidate()` on logout |
| JWT validation | [JwtHelper.validateAndParse](src/main/java/com/ecommerce/helper/JwtHelper.java) | Signature + expiry checked on every protected request |
| JWT blacklist | [AuthFilter.isTokenBlacklisted](src/main/java/com/ecommerce/filter/AuthFilter.java) | Lookup in Redis on every JWT request |
| Admin authorization | [AdminFilter.java](src/main/java/com/ecommerce/filter/AdminFilter.java) | `role.equalsIgnoreCase("ADMIN")` |
| HttpOnly cookie | [AuthServlet.writeJwtCookie](src/main/java/com/ecommerce/controller/AuthServlet.java) | JS cannot read the JWT — XSS-resistant |
| POST for delete | [ReviewServlet](src/main/java/com/ecommerce/controller/ReviewServlet.java) + JSP forms | No state changes via GET |

### Remaining limitations (to mention up-front in the discussion)
1. **No CSRF tokens.** A determined attacker could forge POST forms from another origin. A synchronizer token per session would close this.
2. **JWT blacklist depends on Redis.** If Redis is down at logout, the token survives until its 12-hour expiry. Mitigations: shorten token TTL, or fall back to a DB blacklist.
3. **Plain JDBC DriverManager, no connection pool.** Each request opens a fresh MySQL connection. Fine for course scope; a real app would use HikariCP.
4. **Simple loop for reviews — N+1.** Easy to read, but at scale every product on the home page costs one extra DB query.

---

## 15. Detailed Feature Flows

### Register flow
```
Browser  POST /signup   (name, email, password, confirmPassword)
   │
   ├─► RateLimitFilter   (INCR ratelimit:<ip>)
   ├─► ErrorHandlingFilter
   ├─► (AuthFilter: doesn't match — skipped)
   ├─► (AdminFilter: doesn't match — skipped)
   ▼
AuthServlet.doPost("/signup")
   ▼
AuthService.register
   ├─► ValidationUtil.validateEmail / validatePassword
   ├─► userDAO.findByEmail        (duplicate check)
   ├─► PasswordHelper.hashPassword
   ├─► userDAO.create             (INSERT INTO users)
   ▼
session.setAttribute("user", user)
session.setAttribute("jwt", token)
write `jwt` HttpOnly cookie
   ▼
Browser ◄─302 /─
```

### Login flow
```
Browser  POST /signin  → RateLimit → ErrorHandling → AuthServlet
   ▼
AuthService.login
   ├─► userDAO.findByEmail
   ├─► PasswordHelper.verifyPassword
   ├─► JwtHelper.generateToken
   ▼
session + jwt cookie set
   ▼
Browser ◄─302 /─
```

### Logout flow
```
Browser  POST /logout
   ▼
AuthServlet
   ├─► find token (session > cookie > Bearer header)
   ├─► AuthFilter.blacklistToken(token)   (Redis: jwt:blacklist:<t> TTL = remaining life)
   ├─► clearJwtCookie  (Max-Age=0)
   ├─► session.invalidate()
   ▼
Browser ◄─302 /─
```

### View products flow
```
Browser GET /
   ▼
RateLimit → ErrorHandling → (AuthFilter: doesn't match — public) → ProductServlet
   ▼
ProductService.getProductsWithReviews
   ├─► getAllProducts()
   │     ├─► Redis: GET products:all
   │     │      hit  → JSON → List<Product>
   │     │      miss → productDAO.findAll() → SETEX products:all 300 <json>
   │     └─►
   ├─► for each product: reviewService.getByProduct(productId)   (simple loop)
   ▼
forward → index.jsp
```

### Product details flow
```
Browser GET /products/details?id=<n>
   ▼
RateLimit → ErrorHandling → (AuthFilter: doesn't match — public) → ProductServlet
   ├─► parseId(req.getParameter("id"))
   │     empty   → sendError(400) → 400.jsp
   ├─► productService.getById(id)
   │     empty   → sendError(404) → 404.jsp
   ├─► reviewService.getByProduct(id)
   ▼
forward → product-details.jsp
```

### Add product flow
```
Browser POST /products/add
   ▼
RateLimit → ErrorHandling → AuthFilter → AdminFilter → ProductServlet
   ▼
ProductService.addProduct
   ├─► ValidationUtil.* (name, description, price, imageUrl)
   ├─► productDAO.create
   ├─► RedisHelper.del("products:all")   (cache invalidation)
   ▼
sendRedirect("/admin")
```

### Edit product flow
Same as add. `productService.updateProduct(id, …)` → `productDAO.update` → cache `del`.

### Delete product flow
Same as add. `productService.deleteProduct(id)` → `productDAO.deleteById` (FK cascade deletes reviews) → cache `del`.

### Add review flow
```
Browser POST /reviews/add  (productId, rating, comment)
   ▼
RateLimit → ErrorHandling → AuthFilter (must be logged in) → ReviewServlet.doPost
   ├─► role must be USER (admins are rejected)
   ├─► parse productId + rating
   ├─► reviewService.addReview(...)
   │     ├─► ValidationUtil.*
   │     └─► reviewDAO.save  (INSERT INTO reviews)
   ▼
sendRedirect("/products/details?id=<productId>")
```

### Delete review flow
```
Browser POST /reviews/delete  (form with hidden reviewId)
   ▼
RateLimit → ErrorHandling → AuthFilter → ReviewServlet.doPost
   ├─► parse reviewId  (400 on bad input)
   ├─► reviewService.deleteReview(reviewId, currentUser.id)
   │     └─► reviewDAO.deleteById(reviewId, userId)
   │           DELETE FROM reviews WHERE id=? AND user_id=?
   ▼
sendRedirect(Referer or "/")
```

### Rate limiting flow
```
Browser  any request
   ▼
RateLimitFilter
   ├─► key = "ratelimit:" + clientIp
   ├─► count = jedis.incr(key)
   ├─► if (count == 1) jedis.expire(key, 60)
   ├─► if (count > 30) → 429 JSON, stop chain
   ▼
ErrorHandlingFilter → ... → Servlet
```

---

## 16. Important Code Snippets Explained

### AuthFilter — token sources
```java
String token = extractTokenFromHeader(req);
if (token == null) {
    token = extractTokenFromCookie(req);
}
if (token != null) {
    if (isTokenBlacklisted(token)) { /* 401 */ }
    if (JwtHelper.validateToken(token)) {
        // set req attribute "user" with id + role, then chain.doFilter
    }
}
// session fallback
User sessionUser = (User) req.getSession().getAttribute("user");
if (sessionUser != null) { /* chain.doFilter */ }
// else: redirect /login.jsp  (or 401 JSON)
```

### AdminFilter — role check
```java
User user = (User) req.getAttribute("user");           // set by AuthFilter
if (user == null) user = (User) req.getSession().getAttribute("user");
if (user == null || !"ADMIN".equalsIgnoreCase(user.getRole())) {
    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required");
    return;
}
chain.doFilter(request, response);
```

### RateLimitFilter — Redis counter
```java
try (Jedis jedis = RedisHelper.getPool().getResource()) {
    long count = jedis.incr(key);
    if (count == 1) jedis.expire(key, 60);
    if (count > 30) {
        JsonUtil.writeJson(resp, 429, Map.of("message", "Too Many Requests"));
        return;
    }
}
```

### DBConnection — plain JDBC
```java
public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(URL, USER, PASSWORD);
}
```
URL/user/password come from env vars (`ECOMMERCE_DB_URL`, etc.) with safe local defaults.

### ProductService — cache logic
```java
String cached = RedisHelper.get("products:all");
if (cached != null) return gson.fromJson(cached, listType);
List<Product> products = productDAO.findAll();
RedisHelper.setex("products:all", 300, JsonUtil.toJson(products));
return products;
```
And after a write: `RedisHelper.del("products:all");`.

### ProductService — simple loop for reviews
```java
public Map<Product, List<Review>> getProductsWithReviews() {
    List<Product> products = getAllProducts();
    Map<Product, List<Review>> productsWithReviews = new LinkedHashMap<>();
    for (Product product : products) {
        List<Review> reviews = reviewService.getByProduct(product.getId());
        productsWithReviews.put(product, reviews);
    }
    return productsWithReviews;
}
```

### ReviewServlet — delete via POST with ownership check
```java
@Override
protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    if ("/reviews/delete".equals(req.getServletPath())) {
        handleDeleteReview(req, resp);
        return;
    }
    // ... add review path
}

private void handleDeleteReview(HttpServletRequest req, HttpServletResponse resp) {
    User user = (User) req.getSession().getAttribute("user");
    long reviewId = Long.parseLong(req.getParameter("reviewId"));  // wrapped in try/catch
    reviewService.deleteReview(reviewId, user.getId());
    // SQL: DELETE FROM reviews WHERE id=? AND user_id=?
}
```

### ErrorHandlingFilter — central catch
```java
try {
    chain.doFilter(request, response);
} catch (ValidationException | NumberFormatException ex) {
    sendError(req, resp, 400, "Invalid request parameter");
} catch (UnauthorizedException ex) {
    resp.sendRedirect(req.getContextPath() + "/login.jsp");
} catch (UserNotFoundException ex) {
    sendError(req, resp, 404, "Resource not found");
} catch (RuntimeException ex) {
    LOGGER.log(Level.SEVERE, "Unhandled runtime exception", ex);
    sendError(req, resp, 500, "Internal server error");
}
```

### PasswordHelper — hash + verify
```java
public static String hashPassword(String plain) {
    byte[] salt = new byte[16]; RANDOM.nextBytes(salt);
    byte[] hash = pbkdf2(plain.toCharArray(), salt);
    return base64(salt) + ":" + base64(hash);
}
public static boolean verifyPassword(String plain, String stored) {
    String[] parts = stored.split(":");
    byte[] salt = base64Decode(parts[0]);
    byte[] hash = pbkdf2(plain.toCharArray(), salt);
    return base64(hash).equals(parts[1]);
}
```

### JwtHelper — create + validate
```java
public static String generateToken(Long userId, String role) {
    Date now = new Date();
    return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("role", role)
            .issuedAt(now)
            .expiration(new Date(now.getTime() + 12 * 3600 * 1000L))
            .signWith(KEY).compact();
}
public static boolean validateToken(String token) {
    try { Jwts.parser().verifyWith(KEY).build().parseSignedClaims(token); return true; }
    catch (Exception e) { return false; }
}
```

---

## 17. How to Run the Project

### Required tools
- JDK 17+
- Maven (or use the bundled wrapper `./mvnw`)
- MySQL 8+ running on `localhost:3306`
- Redis 6+ (default port in this project is **6380**, override via env var)
- Apache Tomcat 10 or 11 (Jakarta EE 10)

### 1. Set up the database
```sql
CREATE DATABASE ecommerce CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ecommerce;
SOURCE /path/to/src/main/resources/schema.sql;

-- Make at least one admin
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@example.com';
```

> **Note:** the schema does not seed users. Register one through the UI first, then run the `UPDATE` to promote them to admin.

### 2. Start Redis
On the default port the project expects:
```bash
redis-server --port 6380
```
Or set `ECOMMERCE_REDIS_PORT=6379` to use the standard port.

### 3. Environment variables (all optional — defaults included)
| Variable | Default | Purpose |
|---|---|---|
| `ECOMMERCE_DB_URL` | `jdbc:mysql://localhost:3306/ecommerce` | JDBC URL |
| `ECOMMERCE_DB_USER` | `root` | DB user |
| `ECOMMERCE_DB_PASSWORD` | `password` | DB password |
| `ECOMMERCE_REDIS_HOST` | `localhost` | Redis host |
| `ECOMMERCE_REDIS_PORT` | `6380` | Redis port |
| `ECOMMERCE_JWT_SECRET` | dev placeholder | JWT signing key (set to a real 32+ byte secret in production) |

### 4. Build the WAR
```bash
./mvnw clean package
```
Output: `target/eCommerce-1.0-SNAPSHOT.war`.

### 5. Deploy
Drop the WAR into Tomcat's `webapps/` directory, or use your IDE's "Run on Tomcat" feature.

Open `http://localhost:8080/eCommerce-1.0-SNAPSHOT/`.

---

## 18. Manual Testing Guide

Each test is "do this → expect that."

### A. Register a new account
1. Visit `/register.jsp`. Fill the form, submit.
2. Expect to be redirected to `/`, navbar shows your name.
3. Confirm a `users` row was inserted with a hashed password.

### B. Login / Logout
1. Logout from the navbar, log back in with the same credentials.
2. After logout, the `jwt` cookie should disappear from dev-tools.

### C. Delete account
1. Logged in as a non-admin, visit `/profile`, click "Delete Account."
2. Expect redirect to `/`, navbar shows logged-out state.
3. Confirm DB: the user's row and all their reviews are gone (FK cascade).

### D. View products
1. Hit `/`. You should see product cards and review counts.
2. Click into one — `/products/details?id=<n>`.

### E. Add a review
1. As a `USER` (not admin), open a product detail page.
2. Use the star widget + comment field; submit.
3. The review appears at the top of the list.

### F. Delete a review
1. Find one of your own reviews; click the trash button.
2. Confirm the dialog → review disappears.
3. Inspect the DOM: button is inside a `<form method="post">` not an `<a href>`.

### G. Non-admin tries admin URLs
1. As `USER`, try to POST to `/products/add` via dev-tools.
2. Expect `403` and the `/403.jsp` page.

### H. Admin can manage products
1. Promote your account to ADMIN in DB.
2. Re-login, click "Dashboard" in the navbar.
3. Add / edit / delete a product — each reflected on `/`.

### I. Product cache
1. Run `redis-cli MONITOR` while loading `/`.
2. First load: `GET products:all` (miss) then `SETEX products:all 300 …`.
3. Second load: `GET products:all` only (hit).
4. After admin adds a product: `DEL products:all` then next load is a miss again.

### J. Rate limiting
1. Run `for i in $(seq 1 40); do curl -i http://localhost:8080/eCommerce-1.0-SNAPSHOT/ ; done`.
2. The 31st onward should return 429 JSON until 60 seconds elapse.

### K. Invalid product id
1. Visit `/products/details?id=abc` → expect the `400.jsp` page, not a 500 stack trace.
2. Visit `/products/details?id=999999` → expect `404.jsp`.

### L. JWT cookie present
1. Login. Open dev-tools → Application → Cookies → look for `jwt`.
2. Should be `HttpOnly`, `Path=/<context-path>`.

### M. JWT blacklist after logout
1. Capture the token value from the `jwt` cookie before logout.
2. Logout. Then:
   ```bash
   curl -i -H "Authorization: Bearer <captured-token>" http://localhost:8080/<ctx>/profile
   ```
3. Expect `401` with `"Token has been revoked"`.

### N. Redis down
1. Stop Redis. Refresh `/` — still works (falls back to DB).
2. Try login + logout — still works; only the JWT-revocation step silently fails.

---

## 19. Discussion Preparation

> 25+ questions the instructor may ask, each with a one-paragraph answer.

**1. What is MVC?**
Model-View-Controller — a way of splitting code. **Model** holds the data (POJOs in `model/`). **View** renders it (JSPs). **Controller** handles requests and decides which model and view to use (the servlets in `controller/`). Service and DAO are extra layers we added inside the controller path to keep business logic and SQL out of the servlets.

**2. What is a Servlet?**
A Java class that the servlet container (Tomcat) calls when a request matches its URL pattern. We extend `HttpServlet` and override `doGet` / `doPost`.

**3. What is JSP?**
JavaServer Pages — HTML templates with embedded Java scriptlets. Tomcat compiles them into servlets at runtime. They're our view layer.

**4. What is a DAO?**
Data Access Object — a class that hides SQL behind plain methods. The servlet doesn't write SQL; it calls `productDAO.findAll()`.

**5. What is the Service layer?**
Where business rules live: validation, password hashing, cache invalidation, joining multiple DAO calls together. Sits between controller and DAO.

**6. Why PreparedStatement?**
It uses `?` placeholders and lets the JDBC driver escape values for us. This prevents SQL injection (the most common web attack) and also lets the DB cache the query plan.

**7. What is SQL Injection?**
An attack where the attacker puts SQL syntax inside an input field. If we concatenated strings, the input could change the meaning of the query. PreparedStatement defends against this completely.

**8. What is a session?**
Server-side memory associated with a browser via the `JSESSIONID` cookie. We store the logged-in user there with `session.setAttribute("user", …)`.

**9. What is a cookie?**
A small piece of text the server tells the browser to remember and send back on every request. We use one cookie: `jwt`, marked HttpOnly so JavaScript cannot read it.

**10. What is a JWT?**
A signed token that contains the user's id and role. The server can hand a JWT to a client and the client just hands it back on every request. The signature proves we minted it.

**11. Difference between session and JWT?**
Session = stateful, kept on the server, cleared by `invalidate()`. JWT = stateless, kept by the client, valid until expiry unless we keep a blacklist. We use both in this project.

**12. What does AuthFilter do?**
Runs before protected URLs. It checks for an `Authorization: Bearer` header, then a `jwt` cookie, then the HTTP session. If none authenticate the user, it redirects to login or returns 401 JSON.

**13. What does AdminFilter do?**
After AuthFilter, it checks that `user.role == "ADMIN"`. If not, it returns 403. Used on `/products/add|edit|delete` and `/admin`.

**14. Why Redis?**
Because reading from RAM is much faster than from MySQL. We use it for things that are read often (product list), need to be counted fast (rate limit), or need to be checked on every request (JWT blacklist).

**15. How does caching work here?**
First read: we query MySQL and write the result to Redis with `SETEX products:all 300 <json>`. Subsequent reads return the cached JSON. On writes (add/edit/delete) we `DEL products:all` so the next reader rebuilds it.

**16. How does rate limiting work?**
Every request goes through `RateLimitFilter`. It increments `ratelimit:<ip>` in Redis. On the first hit per window it sets `EXPIRE 60`. If the counter exceeds 30, we return 429.

**17. Why POST for delete?**
Because deletes change state. GET should be safe (a browser could prefetch it). POST also requires a form submission, which is harder to forge from another site than a `<a href>` is.

**18. 401 vs 403?**
401 = "I don't know who you are." 403 = "I know who you are but you can't do this." AuthFilter sends 401; AdminFilter sends 403.

**19. 400 vs 500?**
400 = bad input from the client (invalid product id, missing field). 500 = bug or infrastructure problem on the server (NPE, DB down).

**20. How are passwords stored?**
PBKDF2-HmacSHA256 with a random 16-byte salt per user and 65 536 iterations, producing a 256-bit hash. Stored as `base64(salt):base64(hash)` in `users.password_hash`. We never store the plain password.

**21. Why DBConnection?**
A single place to construct the JDBC connection. If we ever want to change the URL, credentials, or driver, we change one file. It also reads config from environment variables.

**22. Why simple DriverManager instead of HikariCP?**
For a course project we keep the code beginner-friendly. The performance cost of opening a new connection per request is fine at our scale. In production you would absolutely use a pool like HikariCP.

**23. What is the N+1 problem?**
Loading a parent and its children with 1 query for the parents and 1 query per parent for the children — so for N parents you get N+1 queries. It's slow at scale.

**24. Why did we keep the simple loop for reviews?**
Readability and course scope. `getProductsWithReviews` is two short methods, easy to explain. The traffic is tiny, so the extra queries don't matter. The improvement would be a single `WHERE product_id IN (?,?,…)` query.

**25. Where does the JWT actually get used by the browser?**
On login we set an HttpOnly cookie named `jwt`. The browser sends it back automatically on every request. `AuthFilter` reads it from `req.getCookies()` and validates it with `JwtHelper`.

**26. What happens on logout?**
We find the JWT (session → cookie → header), call `AuthFilter.blacklistToken` to write `jwt:blacklist:<token>` to Redis with the token's remaining TTL, send a `jwt` cookie with `Max-Age=0` to delete it, and `session.invalidate()`.

**27. What is HttpOnly?**
A cookie flag that tells the browser "don't expose me to JavaScript." It prevents an XSS payload from stealing the JWT.

**28. Why is filter order important and how is it enforced?**
A wrong order can let unauthenticated users reach admin code. Annotation-based filter order is officially undefined by the spec — so we declare all four filters in `web.xml` in the order RateLimit → ErrorHandling → Auth → Admin.

**29. What would you improve later?**
(1) Add CSRF tokens. (2) Use HikariCP. (3) Eliminate the N+1 with a single JOIN. (4) Add unit tests. (5) Move JWT to a more standard `HttpOnly; SameSite=Strict; Secure` cookie. (6) Tighten the JWT TTL to ~15 minutes with a refresh token.

---

## 20. Final Summary

### What the project demonstrates
- A clean **layered MVC backend** in pure Java + Servlets, no Spring.
- Real **authentication** with both session and JWT, including secure password hashing and a Redis-backed token blacklist.
- **Authorization** via a dedicated filter (admin vs user).
- **Caching** with Redis and proper invalidation.
- **Rate limiting** with Redis counters.
- **Centralized error handling** that doesn't leak internal details.
- **Server-side validation** before every write.

### Strong points
- Filter pipeline is deterministic (web.xml order) and easy to reason about.
- Every SQL statement uses PreparedStatement — no injection risk.
- JWT is end-to-end usable from the browser (HttpOnly cookie path).
- Logout actually revokes the token (Redis blacklist) — most beginner projects skip this.
- Error pages exist for 400 / 403 / 404 / 500; users never see stack traces.

### Weak points (be honest)
- No connection pool (plain DriverManager).
- N+1 query when showing the home page with reviews.
- No CSRF tokens.
- Default JWT secret is a dev placeholder; in production must come from `ECOMMERCE_JWT_SECRET`.
- JWT blacklist only works while Redis is up.
- No automated tests.

### What to say honestly during the discussion
- "I chose simple JDBC because the course scope is the architecture, not connection pooling."
- "I load reviews in a straightforward loop — readable, but I'm aware it's an N+1 pattern."
- "JWT is in an HttpOnly cookie so the browser actually uses it, and logout blacklists it in Redis."
- "Filters live in `web.xml` so their order is deterministic — annotations would have been undefined."
- "Every DAO uses PreparedStatement, so the app is safe against SQL injection."

### One-minute pitch (English)
> This is a backend-focused e-commerce app built with Servlets, JSP, MySQL, and Redis. The architecture is layered: a request goes through filters (rate limit, error handling, auth, admin), reaches a servlet, which calls a service, which calls a DAO. Authentication supports both session and JWT, with the JWT delivered to the browser as an HttpOnly cookie. Logout revokes the token via a Redis blacklist. Products are cached in Redis and invalidated on write. All SQL is parameterized, all passwords are hashed with PBKDF2, and admin-only actions are guarded by a dedicated filter. Errors are handled centrally so users never see stack traces.

### One-minute pitch (Arabic)
> هذا مشروع متجر إلكتروني مركّز على الـ Backend، مكتوب بلغة جافا باستخدام Servlets و JSP، مع قاعدة بيانات MySQL وذاكرة كاش Redis. المعمارية مقسّمة لطبقات: الطلب يمرّ بسلسلة فلاتر (تحديد المعدّل، معالجة الأخطاء، المصادقة، صلاحيات الأدمن) ثم يدخل على الـ Servlet الذي يستدعي طبقة الـ Service ومن بعدها الـ DAO. نظام المصادقة يدعم الجلسة (Session) و JWT في نفس الوقت، والـ JWT يُحفظ في كوكي HttpOnly عشان المتصفح يستخدمه تلقائيًا. عند الـ Logout بنضيف التوكين لقائمة سوداء في Redis. المنتجات بتتخزن في Redis كـ Cache وبنمسح الكاش بعد أي إضافة أو تعديل أو حذف. كل استعلامات الـ SQL باستخدام PreparedStatement، وكلمات السر بتُخزّن مشفّرة بـ PBKDF2، وصفحات الإدارة محميّة بـ AdminFilter. أي خطأ بيتم التعامل معه مركزياً عشان المستخدم ما يشوفش تفاصيل داخلية.
