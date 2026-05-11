-- =============================================================
--  Inventory Warehouse Information System – Database Schema
--  MySQL 8.x
--  OOP Part 2 | Winter 2022/2023 | Topic V: Inventory Warehouse
-- =============================================================

-- Create and select the database
CREATE DATABASE IF NOT EXISTS warehouse_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE warehouse_db;

-- -----------------------------------------------------------
-- TABLE: users
-- Stores Admin and Operator accounts
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,       -- BCrypt hash
    full_name   VARCHAR(120) NOT NULL,
    email       VARCHAR(120),
    role        ENUM('ADMIN','OPERATOR') NOT NULL DEFAULT 'OPERATOR',
    active      TINYINT(1) NOT NULL DEFAULT 1,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- TABLE: suppliers
-- Companies / persons that deliver goods to the warehouse
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS suppliers (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    contact     VARCHAR(100),
    address     VARCHAR(255),
    phone       VARCHAR(30),
    email       VARCHAR(120),
    active      TINYINT(1) NOT NULL DEFAULT 1,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- TABLE: clients
-- Companies / persons who purchase goods from the warehouse
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS clients (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    contact     VARCHAR(100),
    address     VARCHAR(255),
    phone       VARCHAR(30),
    email       VARCHAR(120),
    active      TINYINT(1) NOT NULL DEFAULT 1,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- TABLE: goods  (nomenclatures / products)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS goods (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(200) NOT NULL,
    category        VARCHAR(100),
    unit            VARCHAR(30)  NOT NULL DEFAULT 'pcs',  -- e.g. pcs, kg, ltr
    delivery_price  DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    sales_price     DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    quantity        INT NOT NULL DEFAULT 0,
    min_threshold   INT NOT NULL DEFAULT 5,               -- alert below this
    active          TINYINT(1) NOT NULL DEFAULT 1,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- TABLE: invoices
-- Header record for both incoming (PURCHASE) and outgoing (SALE)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS invoices (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    invoice_number  VARCHAR(50)  NOT NULL UNIQUE,
    type            ENUM('PURCHASE','SALE') NOT NULL,
    invoice_date    DATE NOT NULL,
    total_amount    DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    supplier_id     INT NULL,
    client_id       INT NULL,
    operator_id     INT NOT NULL,                         -- who created it
    notes           TEXT,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_invoice_supplier FOREIGN KEY (supplier_id)  REFERENCES suppliers(id),
    CONSTRAINT fk_invoice_client   FOREIGN KEY (client_id)    REFERENCES clients(id),
    CONSTRAINT fk_invoice_operator FOREIGN KEY (operator_id)  REFERENCES users(id)
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- TABLE: invoice_items
-- Line items for each invoice
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS invoice_items (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id  INT NOT NULL,
    good_id     INT NOT NULL,
    quantity    INT NOT NULL,
    unit_price  DECIMAL(12,2) NOT NULL,
    subtotal    DECIMAL(14,2) GENERATED ALWAYS AS (quantity * unit_price) STORED,

    CONSTRAINT fk_item_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_good   FOREIGN KEY (good_id)    REFERENCES goods(id)
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- TABLE: cash_registers
-- One or more cash registers; balances tracked here
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS cash_registers (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL DEFAULT 'Main Register',
    balance         DECIMAL(14,2) NOT NULL DEFAULT 0.00,
    min_threshold   DECIMAL(14,2) NOT NULL DEFAULT 100.00,  -- alert below
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- TABLE: cash_transactions
-- Every credit / debit movement for a cash register
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS cash_transactions (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    register_id     INT NOT NULL,
    type            ENUM('INCOME','EXPENSE','DEPOSIT','WITHDRAWAL') NOT NULL,
    amount          DECIMAL(14,2) NOT NULL,
    description     VARCHAR(255),
    invoice_id      INT NULL,                              -- linked invoice if any
    operator_id     INT NOT NULL,
    transaction_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ct_register FOREIGN KEY (register_id)  REFERENCES cash_registers(id),
    CONSTRAINT fk_ct_invoice  FOREIGN KEY (invoice_id)   REFERENCES invoices(id),
    CONSTRAINT fk_ct_operator FOREIGN KEY (operator_id)  REFERENCES users(id)
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- TABLE: activity_log
-- Operator / admin action audit trail (supplement to log4j file)
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS activity_log (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT NULL,
    username    VARCHAR(50),
    action      VARCHAR(200) NOT NULL,
    details     TEXT,
    log_date    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- -----------------------------------------------------------
-- INDEXES for performance
-- -----------------------------------------------------------
CREATE INDEX idx_goods_category  ON goods(category);
CREATE INDEX idx_invoices_date   ON invoices(invoice_date);
CREATE INDEX idx_invoices_type   ON invoices(type);
CREATE INDEX idx_ct_date         ON cash_transactions(transaction_date);
CREATE INDEX idx_log_user        ON activity_log(user_id);
CREATE INDEX idx_log_date        ON activity_log(log_date);

-- -----------------------------------------------------------
-- SEED DATA
-- -----------------------------------------------------------

-- Default admin account  (password = "admin123" – BCrypt)
INSERT INTO users (username, password, full_name, role) VALUES
('admin',
 '$2a$12$Whi.y3TCh7YRWVPMhCEoEeIbZpbF8xhfN6v5jUrJJiM5.9yFIcXKy',
 'System Administrator',
 'ADMIN');

-- Default operator account  (password = "oper123" – BCrypt)
INSERT INTO users (username, password, full_name, role) VALUES
('operator1',
 '$2a$12$jOdL8gSH5HLjWEJh2BtFcOJWYDKHKqr1yYnVLRe1MCE53LXqrjKii',
 'Warehouse Operator One',
 'OPERATOR');

-- Default cash register
INSERT INTO cash_registers (name, balance, min_threshold) VALUES ('Main Register', 5000.00, 200.00);

-- Sample suppliers
INSERT INTO suppliers (name, contact, address, phone, email) VALUES
('Alpha Supplies Ltd.',  'John Alpha',  '12 Supply St, Sofia',   '+359 2 111 2222', 'alpha@example.com'),
('Beta Wholesale Co.',   'Maria Beta',  '34 Wholesale Ave, Plovdiv', '+359 32 333 4444', 'beta@example.com');

-- Sample clients
INSERT INTO clients (name, contact, address, phone, email) VALUES
('Gamma Retail Shop',   'Peter Gamma', '56 Retail Rd, Varna',   '+359 52 555 6666', 'gamma@example.com'),
('Delta Markets Ltd.',  'Anna Delta',  '78 Market Blvd, Burgas', '+359 56 777 8888', 'delta@example.com');

-- Sample goods
INSERT INTO goods (name, category, unit, delivery_price, sales_price, quantity, min_threshold) VALUES
('Laptop 15" Pro',        'Electronics',  'pcs',  800.00, 1200.00,  30, 5),
('Wireless Mouse',        'Electronics',  'pcs',    8.00,   15.00, 150, 20),
('Office Chair Deluxe',   'Furniture',    'pcs',   80.00,  130.00,  25, 3),
('A4 Paper Ream 500sh',   'Stationery',   'pcs',    2.50,    4.50, 400, 50),
('Ink Cartridge Black',   'Stationery',   'pcs',    5.00,    9.00,  80, 15),
('USB-C Hub 7-port',      'Electronics',  'pcs',   15.00,   28.00,  60, 10),
('Ballpoint Pen (box)',   'Stationery',   'box',    1.20,    2.50, 200, 30),
('Desk Lamp LED',         'Furniture',    'pcs',   18.00,   30.00,  45, 8);

