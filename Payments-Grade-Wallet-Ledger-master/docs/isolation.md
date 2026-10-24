# Isolation lab notes

## Read Committed

Observation: a second session can update the same row after Session A runs a read. Session A does not see a snapshot; it sees the latest committed value when it re-reads the row.

Example:

```sql
-- Session A
SELECT balance FROM accounts WHERE id = 1;

-- Session B
UPDATE accounts SET balance = balance - 100 WHERE id = 1;
COMMIT;

-- Session A
SELECT balance FROM accounts WHERE id = 1;
```

Result: Session A can see a different value after Session B commits.

## Repeatable Read

Postgres gives a snapshot for the transaction. The same query repeated within the same transaction still sees the same snapshot, even if another transaction commits.

## Serializable

Two transactions each read and then write based on stale values. One transaction will abort with a serialization failure and must retry.

Example:

```sql
-- Session A
BEGIN;
SELECT balance FROM accounts WHERE id = 1;
-- Session B does same
-- both try UPDATE ...
COMMIT;
```

One of them fails with a serialization error.

## Why the wallet transfer uses row locks

The transfer service does:

```sql
SELECT balance FROM accounts WHERE id = ? FOR UPDATE;
```

This prevents two transfers from reading the same wallet balance and writing a stale value at the same time. The lock keeps the race from producing overdrafts or lost updates.
