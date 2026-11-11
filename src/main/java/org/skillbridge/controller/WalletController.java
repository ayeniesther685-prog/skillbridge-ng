package org.skillbridge.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skillbridge.dto.request.WithdrawalRequestCreateRequest;
import org.skillbridge.dto.response.WalletResponse;
import org.skillbridge.dto.response.WalletTransactionResponse;
import org.skillbridge.dto.response.WithdrawalResponse;
import org.skillbridge.service.impl.WalletServiceImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@Tag(name = "Wallet")
public class WalletController {

    private final WalletServiceImpl service;

    public WalletController(WalletServiceImpl service) { this.service = service; }

    @GetMapping("/me")
    public ResponseEntity<WalletResponse> myWallet() {
        return ResponseEntity.ok(service.getMyWallet());
    }

    @GetMapping("/me/transactions")
    public ResponseEntity<Page<WalletTransactionResponse>> myTransactions(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getMyTransactions(pageable));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<WithdrawalResponse> requestWithdrawal(
            @Valid @RequestBody WithdrawalRequestCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.requestWithdrawal(req));
    }

    @GetMapping("/me/withdrawals")
    public ResponseEntity<Page<WithdrawalResponse>> myWithdrawals(
            @PageableDefault(size = 20, sort = "requestedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getMyWithdrawals(pageable));
    }
}