package com.wallet.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wallet.service.WalletService;

@RestController
public class WalletController {
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/users")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, Object> request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(walletService.createUser(
                        String.valueOf(request.get("name")),
                        String.valueOf(request.get("email")),
                        String.valueOf(request.get("phone")),
                        String.valueOf(request.get("kyc_status"))));
    }

    @PostMapping("/users/{userId}/wallets")
    public ResponseEntity<Map<String, Object>> createWallet(@PathVariable Long userId,
            @RequestBody Map<String, Object> request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(walletService.createWallet(userId, String.valueOf(request.getOrDefault("code", "main-wallet")),
                        String.valueOf(request.getOrDefault("currency", "INR"))));
    }

    @PostMapping("/wallets/{walletId}/topup")
    public ResponseEntity<Map<String, Object>> topUp(@PathVariable Long walletId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(walletService.topUp(walletId, Long.valueOf(String.valueOf(request.get("amount"))),
                idempotencyKey, String.valueOf(request.getOrDefault("notes", ""))));
    }

    @PostMapping("/wallets/transfer")
    public ResponseEntity<Map<String, Object>> transfer(@RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(walletService.transfer(
                Long.valueOf(String.valueOf(request.get("from_wallet_id"))),
                Long.valueOf(String.valueOf(request.get("to_wallet_id"))),
                Long.valueOf(String.valueOf(request.get("amount"))),
                String.valueOf(request.getOrDefault("transfer_type", "peer_transfer")),
                idempotencyKey,
                String.valueOf(request.getOrDefault("notes", ""))));
    }

    @GetMapping("/wallets/{walletId}/balance")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable Long walletId) {
        return ResponseEntity.ok(Map.of("wallet_id", walletId, "balance", walletService.getBalance(walletId)));
    }

    @GetMapping("/wallets/{walletId}/statement")
    public ResponseEntity<Map<String, Object>> getStatement(@PathVariable Long walletId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return ResponseEntity
                .ok(Map.of("wallet_id", walletId, "entries", walletService.getStatement(walletId, limit, offset)));
    }

    @PostMapping("/admin/reconciliation/run")
    public ResponseEntity<Map<String, Object>> runReconciliation() {
        return ResponseEntity.ok(walletService.runReconciliation());
    }

    @PostMapping("/users/{walletId}/statement/ask")
    public ResponseEntity<Map<String, Object>> askStatementQuestion(@PathVariable Long walletId,
            @RequestBody Map<String, Object> request) {
        return ResponseEntity
                .ok(walletService.askStatementQuestion(walletId, String.valueOf(request.getOrDefault("question", ""))));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
