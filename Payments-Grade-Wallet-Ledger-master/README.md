# Payments Grade Wallet Ledger

This project implements a double-entry digital wallet backend in Java with Spring Boot and PostgreSQL.

## Design choices

- Money is stored in whole paise as `BIGINT` values.
- Every movement is recorded in two ledger entries: one debit and one credit.
- Wallet balances are stored on the account row for fast reads, but the canonical truth remains the ledger.
- Transfer code is explicit and uses SQL with transaction boundaries so the money path is visible.

## Assumptions

- `INR` is the only supported currency in this version.
- A user may own multiple wallet accounts.
- System accounts are stored in the same `accounts` table as wallet accounts to keep the ledger consistent.

## Notes

- Balance checks are done with explicit SQL and row locks inside a transaction.
- Idempotency is enforced by `(user_id, key_value)` uniqueness.
- Reconciliation is a nightly job concept implemented as a service endpoint and SQL-based validation.

## Run locally

```bash
mvn clean package
java -jar target/wallet-ledger-1.0.0.jar
```

or with Docker:

```bash
docker compose up --build
```

## API examples

```bash
curl -X POST http://localhost:8080/users \
  -H 'Content-Type: application/json' \
  -d '{"name":"Asha","email":"asha@example.com","phone":"9999000001","kyc_status":"verified"}'

curl -X POST http://localhost:8080/users/1/wallets \
  -H 'Content-Type: application/json' \
  -d '{"code":"main-wallet","currency":"INR"}'

curl -X POST http://localhost:8080/wallets/1/topup \
  -H 'Idempotency-Key: abc-123' \
  -H 'Content-Type: application/json' \
  -d '{"amount":5000,"notes":"top up"}'

curl -X POST http://localhost:8080/wallets/transfer \
  -H 'Idempotency-Key: transfer-xyz' \
  -H 'Content-Type: application/json' \
  -d '{"from_wallet_id":1,"to_wallet_id":2,"amount":2500,"transfer_type":"peer_transfer"}'
```
