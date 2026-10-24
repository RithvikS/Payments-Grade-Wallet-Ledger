package com.wallet;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.wallet.service.WalletService;

@SpringBootTest
@ActiveProfiles("test")
class WalletServiceTests {

    @Autowired
    private WalletService walletService;

    @Test
    void topUpAndTransferShouldBalanceBooks() {
        Map<String, Object> user = walletService.createUser("Asha", "asha@example.com", "9999000001", "verified");
        Map<String, Object> walletA = walletService.createWallet(Long.valueOf(String.valueOf(user.get("id"))),
                "main-wallet", "INR");
        Map<String, Object> walletB = walletService.createWallet(Long.valueOf(String.valueOf(user.get("id"))),
                "savings-wallet", "INR");

        walletService.topUp(Long.valueOf(String.valueOf(walletA.get("id"))), 5000L, "topup-1", "seed");
        Map<String, Object> transfer = walletService.transfer(
                Long.valueOf(String.valueOf(walletA.get("id"))),
                Long.valueOf(String.valueOf(walletB.get("id"))),
                2000L,
                "peer_transfer",
                "transfer-1",
                "moving funds");

        assertThat(transfer.get("status")).isEqualTo("posted");
        assertThat(walletService.getBalance(Long.valueOf(String.valueOf(walletA.get("id"))))).isEqualTo(3000L);
        assertThat(walletService.getBalance(Long.valueOf(String.valueOf(walletB.get("id"))))).isEqualTo(2000L);
    }

    @Test
    void sameRequestIdempotencyShouldNotDuplicateTransfer() {
        Map<String, Object> user = walletService.createUser("Rahul", "rahul@example.com", "9999000002", "verified");
        Map<String, Object> walletA = walletService.createWallet(Long.valueOf(String.valueOf(user.get("id"))),
                "main-wallet", "INR");
        Map<String, Object> walletB = walletService.createWallet(Long.valueOf(String.valueOf(user.get("id"))),
                "secondary-wallet", "INR");

        walletService.topUp(Long.valueOf(String.valueOf(walletA.get("id"))), 10000L, "topup-idempotent", "seed");
        Map<String, Object> first = walletService.transfer(
                Long.valueOf(String.valueOf(walletA.get("id"))),
                Long.valueOf(String.valueOf(walletB.get("id"))),
                1000L,
                "peer_transfer",
                "retry-1",
                "same-key");
        Map<String, Object> second = walletService.transfer(
                Long.valueOf(String.valueOf(walletA.get("id"))),
                Long.valueOf(String.valueOf(walletB.get("id"))),
                1000L,
                "peer_transfer",
                "retry-1",
                "same-key");

        assertThat(first).isEqualTo(second);
    }
}
