CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(30) NOT NULL,
    kyc_status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS accounts (
    id BIGSERIAL PRIMARY KEY,
    kind VARCHAR(20) NOT NULL CHECK (kind IN ('wallet', 'system')),
    code VARCHAR(100),
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    balance BIGINT NOT NULL DEFAULT 0,
    user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(kind, code)
);

CREATE TABLE IF NOT EXISTS transfers (
    id BIGSERIAL PRIMARY KEY,
    transfer_type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    amount BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT
);

CREATE TABLE IF NOT EXISTS ledger_entries (
    id BIGSERIAL PRIMARY KEY,
    transfer_id BIGINT NOT NULL REFERENCES transfers(id),
    account_id BIGINT NOT NULL REFERENCES accounts(id),
    direction VARCHAR(10) NOT NULL CHECK (direction IN ('debit', 'credit')),
    amount BIGINT NOT NULL CHECK (amount > 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ledger_account_time ON ledger_entries(account_id, created_at DESC, id DESC);
CREATE INDEX IF NOT EXISTS idx_transfer_time ON transfers(created_at DESC, id DESC);

CREATE TABLE IF NOT EXISTS idempotency_keys (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    key_value VARCHAR(255) NOT NULL,
    request_hash VARCHAR(128) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    response_code INTEGER,
    response_body TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, key_value)
);

CREATE TABLE IF NOT EXISTS reconciliation_runs (
    id BIGSERIAL PRIMARY KEY,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    problems_found INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS reconciliation_issues (
    id BIGSERIAL PRIMARY KEY,
    run_id BIGINT NOT NULL REFERENCES reconciliation_runs(id),
    check_name VARCHAR(100) NOT NULL,
    entity_id VARCHAR(50),
    expected_value VARCHAR(255),
    actual_value VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    transfer_id BIGINT NOT NULL REFERENCES transfers(id),
    old_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by VARCHAR(100) NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS risk_rules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    sql_query TEXT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS risk_flags (
    id BIGSERIAL PRIMARY KEY,
    transfer_id BIGINT NOT NULL REFERENCES transfers(id),
    rule_name VARCHAR(100) NOT NULL,
    explanation TEXT,
    label VARCHAR(30) NOT NULL DEFAULT 'review',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS job_locks (
    id BIGSERIAL PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL UNIQUE,
    locked_at TIMESTAMP
);

INSERT INTO accounts(kind, code, currency, balance) VALUES
('system', 'cash_in', 'INR', 0),
('system', 'cash_out', 'INR', 0),
('system', 'fee_revenue', 'INR', 0)
ON CONFLICT (kind, code) DO NOTHING;
