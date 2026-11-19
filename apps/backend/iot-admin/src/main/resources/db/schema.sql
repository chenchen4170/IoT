CREATE TABLE IF NOT EXISTS iot_user (
    id INTEGER PRIMARY KEY,
    username TEXT NOT NULL COLLATE NOCASE UNIQUE
        CHECK (username = TRIM(username) AND username <> ''),
    password_hash TEXT NOT NULL CHECK (password_hash <> ''),
    create_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
    update_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
)^^

CREATE TABLE IF NOT EXISTS product_category (
    id INTEGER PRIMARY KEY,
    parent_id INTEGER NOT NULL DEFAULT 0,
    category_code TEXT NOT NULL,
    category_name TEXT NOT NULL,
    description TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    status INTEGER NOT NULL DEFAULT 1,
    is_deleted INTEGER NOT NULL DEFAULT 0,
    created_by INTEGER,
    updated_by INTEGER,
    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
)^^

CREATE UNIQUE INDEX IF NOT EXISTS uk_product_category_code
    ON product_category (category_code)^^
