# SurakshaSetu 🛡️

> **Digital Identity & Trust Management System for Gig Economy Workers**  
> Stack: Java 11 + Swing + JDBC + MySQL | No external libraries

---

## Project Structure

```
SurakshaSetu/
├── sql/
│   ├── schema.sql          ← 15-table DDL
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
│   └── mysql-connector-j-9.0.0.jar   ← Place here!
├── out/                   ← Created by compile.bat
├── compile.bat
└── run.bat
```

---

## Prerequisites

| Requirement | Version |
|-------------|---------|
| JDK         | 11+     |
| MySQL Server| 8.0+    |
| MySQL Connector/J | 9.0.0 |

---

## Setup Instructions

### Step 1 — Download MySQL Connector JAR
Download from: https://dev.mysql.com/downloads/connector/j/  
Place `mysql-connector-j-9.0.0.jar` in the `lib/` folder.

### Step 2 — Create Database
Open MySQL Workbench or run in terminal:
```bash
mysql -u root -p < sql/schema.sql
mysql -u root -p < sql/sample_data.sql
```
Or in MySQL shell:
```sql
SOURCE C:/Users/shrey/dbms_prj/SurakshaSetu/sql/schema.sql;
SOURCE C:/Users/shrey/dbms_prj/SurakshaSetu/sql/sample_data.sql;
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
| Work Logger    | Log daily work entries with platform, hours, earnings |
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
  0.4 × (workDaysLast30 / 30) × 100          ← Consistency
+ 0.3 × avgRating × 20                         ← Rating (0-5 → 0-100)
+ 0.3 × (minMonthlyIncome / avgMonthlyIncome)  ← Income Stability

Final = weightedSum × 10
```
Score changes are persisted atomically with audit records via MySQL transactions.
