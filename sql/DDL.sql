-- DDL.sql
-- AIS Cafeteria POS (Assignment 2)
-- Target DB: SQLite (works on most SQL engines with small tweaks)

PRAGMA foreign_keys = ON;

-- Staff (optional but useful for staffId on orders)
CREATE TABLE IF NOT EXISTS staff (
    staff_id    TEXT PRIMARY KEY,
    full_name   TEXT NOT NULL,
    role        TEXT NOT NULL DEFAULT 'Staff',
    is_active   INTEGER NOT NULL DEFAULT 1 CHECK (is_active IN (0, 1))
);

-- Menu items (matches com.ais.cafeteria.pos.models.MenuItem)
CREATE TABLE IF NOT EXISTS menu_item (
    menu_item_id    INTEGER PRIMARY KEY,
    name            TEXT NOT NULL,
    description     TEXT,
    price           REAL NOT NULL CHECK (price >= 0),
    category        TEXT NOT NULL,
    emoji           TEXT,
    is_available    INTEGER NOT NULL DEFAULT 1 CHECK (is_available IN (0, 1)),
    created_at      TEXT NOT NULL DEFAULT (datetime('now')),
    updated_at      TEXT
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_menu_item_name ON menu_item(name);
CREATE INDEX IF NOT EXISTS ix_menu_item_category ON menu_item(category);

-- Orders (matches com.ais.cafeteria.pos.models.Order)
CREATE TABLE IF NOT EXISTS "order" (
    order_id        TEXT PRIMARY KEY,
    order_date      TEXT NOT NULL, -- store ISO-8601 text
    total           REAL NOT NULL CHECK (total >= 0),
    payment_method  TEXT NOT NULL CHECK (payment_method IN ('Cash', 'Card', 'Wallet')),
    status          TEXT NOT NULL CHECK (status IN ('Pending', 'Completed', 'Cancelled')),
    note            TEXT NOT NULL DEFAULT '',
    staff_id        TEXT,
    created_at      TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (staff_id) REFERENCES staff(staff_id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS ix_order_date ON "order"(order_date);
CREATE INDEX IF NOT EXISTS ix_order_staff ON "order"(staff_id);

-- Order items (breaks Order.items (List<CartItem>) into a relational table)
CREATE TABLE IF NOT EXISTS order_item (
    order_item_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    order_id        TEXT NOT NULL,
    menu_item_id    INTEGER NOT NULL,
    quantity        INTEGER NOT NULL CHECK (quantity > 0),
    unit_price      REAL NOT NULL CHECK (unit_price >= 0),
    line_total      REAL NOT NULL CHECK (line_total >= 0),
    FOREIGN KEY (order_id) REFERENCES "order"(order_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_item(menu_item_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS ix_order_item_order ON order_item(order_id);
CREATE INDEX IF NOT EXISTS ix_order_item_menu_item ON order_item(menu_item_id);

