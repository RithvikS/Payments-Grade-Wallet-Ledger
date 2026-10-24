# Schema design notes

## Noun list

- user
- wallet
- system account
- currency
- transfer
- ledger entry
- idempotency key
- reconciliation run
- reconciliation issue
- audit log
- risk rule
- risk flag
- job lock

## Decisions

- A user becomes a table because we store name, email, phone, KYC, and creation time.
- A wallet is attached to one user and appears as an account row with `kind='wallet'`.
- System accounts also live in `accounts` because the ledger must treat them like any other account.
- A transfer is a business event with type, status, amount, and timestamps.
- Ledger entries are a separate table because every debit and credit is immutable and must be traced.
- Idempotency keys are stored per user to stop duplicate requests after timeouts.
- Reconciliation rows are stored to record nightly book checks.
- Audit logs track status changes with who changed them and when.

## ER overview

```text
users 1 --- * accounts
accounts 1 --- * ledger_entries
transfers 1 --- * ledger_entries
users 1 --- * idempotency_keys
transfers 1 --- * audit_logs
transfers 1 --- * risk_flags
reconciliation_runs 1 --- * reconciliation_issues
```

## Why balances are stored and computed

The app stores a cached balance on the account row for fast reads, but the ledger remains the source of truth. This is the classic trade-off: writes are a little slower because `balance` must be updated in the same transaction as the ledger rows, but reads become much cheaper.
