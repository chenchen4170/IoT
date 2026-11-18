CREATE TABLE IF NOT EXISTS iot_user (
    id INTEGER PRIMARY KEY,
    username TEXT NOT NULL COLLATE NOCASE UNIQUE
        CHECK (username = TRIM(username) AND username <> ''),
    password_hash TEXT NOT NULL CHECK (password_hash <> ''),
    create_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
    update_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
)^^

