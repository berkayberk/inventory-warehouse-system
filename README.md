# Inventory Warehouse Information System

A desktop application built with **Java 17 + JavaFX 17 + MySQL 8** for the OOP Part 2 university course (Winter 2022/2023).

---

## Features

| Module                     | Admin | Operator  |
| -------------------------- | :---: | :-------: |
| Login / Logout             |  ✅   |    ✅     |
| User Management (CRUD)     |  ✅   |    ❌     |
| Supplier Management (CRUD) |  ✅   |    ❌     |
| Client Management (CRUD)   |  ✅   |    ❌     |
| Goods Management (CRUD)    |  ✅   | View only |
| Purchase Invoice           |  ✅   |    ✅     |
| Sale Invoice               |  ✅   |    ✅     |
| Invoice List               |  ✅   |    ✅     |
| Cash Register (view)       |  ✅   |    ✅     |
| Cash Deposit / Withdraw    |  ✅   |    ❌     |
| Reports                    |  ✅   |    ❌     |
| Activity Log               |  ✅   |    ❌     |

---

## Technology Stack

- **Java 17** · **JavaFX 17.0.6** (FXML)
- **MySQL 8** (JDBC, no ORM) · DAO pattern
- **Apache Log4j 2** · **BCrypt** passwords
- **JUnit 5** + **Mockito** · **Apache Maven**

---

## Quick Start

### 1. Database

```bash
mysql -u root -p < sql/schema.sql
```

Edit `src/main/java/com/warehouse/util/DatabaseConnection.java` if your MySQL password differs from `root`.

### 2. Run

```bash
mvn javafx:run
```

### 3. Default Credentials

| Username  | Password | Role     |
| --------- | -------- | -------- |
| admin     | admin123 | ADMIN    |
| operator1 | oper123  | OPERATOR |

---

## Project Structure

```
src/main/java/com/warehouse/
├── MainApp.java
├── model/          # Domain objects
├── dao/            # DAO interfaces + implementations
├── service/        # Business logic
├── controller/     # JavaFX controllers
└── util/           # DB, session, password, alerts

src/main/resources/
├── fxml/           # FXML views
├── css/styles.css
└── log4j2.xml

sql/schema.sql      # Full DB schema + seed data
docs/               # Academic documentation & diagrams
```

---

## Running Tests

```bash
mvn test
```
