# SurakshaSetu 🛡️

> **Digital Identity & Trust Management System for Gig Economy Workers**  
> Stack: Java 11 + Swing + JDBC + PostgreSQL | No external libraries

---

## Project Structure

```
SurakshaSetu/
├── sql/
│   ├── schema.sql          ← 15-table DDL (PostgreSQL)
│   └── sample_data.sql     ← 5 workers, 20+ logs
├── src/
│   └── com/suraksha/setu/
│       ├── SurakshasetuApp.java  ← Entry point
│       ├── exceptions/           ← 4 custom exceptions
│       ├── util/                 ← DB, ID gen, password, filter
│       ├── models/               ← 10 domain classes
│       ├── dao/                  ← 8 JDBC DAO classes
│       ├── services/             ← 6 business logic classes
│       └── ui/                   ← 9 Swing windows/panels
├── lib/
│   └── postgresql-42.7.3.jar   ← Place here!
├── out/                   ← Created by compile.bat
├── compile.bat
└── run.bat
```

---

## Prerequisites

| Requirement | Version |
|-------------|---------|
| JDK         | 11+     |
| PostgreSQL  | 12.0+   |
| PostgreSQL JDBC Driver | 42.7.3 |

---

## Setup Instructions

### Step 1 — Download PostgreSQL JDBC Driver
Download from: https://jdbc.postgresql.org/download/  
Place `postgresql-42.7.3.jar` in the `lib/` folder.

### Step 2 — Create Database
Open pgAdmin 4 or run in terminal:
```bash
psql -U postgres -d suraksha_setu -f sql/schema.sql
psql -U postgres -d suraksha_setu -f sql/sample_data.sql
```
Or in psql shell:
```sql
\i C:/Users/shrey/dbms_prj/SurakshaSetu/sql/schema.sql;
\i C:/Users/shrey/dbms_prj/SurakshaSetu/sql/sample_data.sql;
```

### Step 3 — Compile
```
compile.bat
```

### Step 4 — Run
```
run.bat
```

---

## Login Credentials

| Username  | Password    | Role   |
|-----------|-------------|--------|
| rajesh_k  | password123 | Worker |
| priya_s   | password123 | Worker |
| amit_sh   | password123 | Worker |
| kavya_r   | password123 | Worker |
| irfan_m   | password123 | Worker |
| admin     | admin123    | Admin  |

> **Note:** Passwords are hashed with a custom rolling-hash function (no external lib).  
> To re-hash for DB, run: `PasswordHasher.hash("password123")` — then update app_users.

---

## Features

| Tab            | Functionality |
|----------------|---------------|
| Dashboard      | Profile card, trust score gauge, tier badge, KPIs |
| Work Logger    | Log daily work entries with platform, hours, earnings, and delete mistaken entries |
| Financial Hub  | Monthly summary table, net income calculation |
| Loan Eligibility | Check eligibility, browse providers, apply for loans |
| Admin Panel    | Search/filter/sort all workers, batch recalculation |

**Dialogs:**
- **Work Certificate** — StringBuffer-generated text cert
- **Audit Trail** — Full trust score change history

---

## Java Concepts Demonstrated

| Unit | Concepts |
|------|----------|
| I | Classes, constructors, overloading, `final`, `static`, inheritance, `abstract`, `super()`, `interface`, custom exceptions |
| II | `String` manipulation, `StringBuffer`, type wrappers, autoboxing, lambdas, `@FunctionalInterface`, `Thread`, `join()`, `isAlive()`, `synchronized` |
| III | `ArrayList`, `HashMap`, `HashSet`, `PriorityQueue`, `Iterator`, `Spliterator`, `Comparable`, `Comparator`, `GenericDAO<T>`, stream API |
| IV | `JFrame`, `JTabbedPane`, `JTable`, `JComboBox`, `JSpinner`, `JMenuBar`, `ActionListener`, `ItemListener`, `DocumentListener`, `WindowListener`, JDBC, `PreparedStatement`, `ResultSet`, `SQLException`, DAO pattern |
| DB | 15 normalized tables, FK constraints, indexes, transactions (`COMMIT`/`ROLLBACK`) |

---

## Architecture

```
LoginFrame → AuthService → AppUserDAO
                ↓
           MainFrame (JTabbedPane)
           ├── WorkerDashboardPanel → TrustScoreService → TrustScoreDAO (transactions)
           ├── WorkLoggerPanel      → WorkHistoryDAO
           ├── FinancialHubPanel    → ReportService (stream/lambda)
           ├── LoanEligibilityPanel → LoanEligibilityService → LoanApplicationDAO
           └── AdminPanel           → WorkerDAO (HashMap, sort, lambda filter)

Background: TrustScoreUpdater (Thread) → TrustScoreService (synchronized)
```

---

## Trust Score Formula

```
Score (0–1000) =
  0.40 × Consistency (blend of distinct work days + total gigs logged in last 90 days)
+ 0.35 × RatingScore (avgRating × 20)
+ 0.25 × IncomeScore (incomeLevel + regularityBonus)

Final = weightedSum × 10
```
Score changes are persisted atomically with audit records via PostgreSQL transactions.
