package com.example.skillswap.controller;

import com.example.skillswap.entity.CreditLedger;
import com.example.skillswap.service.CreditLedgerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ledger")
public class CreditLedgerController {

    private final CreditLedgerService creditLedgerService;

    public CreditLedgerController(CreditLedgerService creditLedgerService) {
        this.creditLedgerService = creditLedgerService;
    }

    // GET /api/ledger/member/{memberId} — Get all transactions for a member
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<CreditLedger>> getLedgerByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(creditLedgerService.getLedgerByMember(memberId));
    }

    // GET /api/ledger/member/{memberId}/balance — Get current balance
    @GetMapping("/member/{memberId}/balance")
    public ResponseEntity<Map<String, Object>> getBalanceByMember(@PathVariable Long memberId) {
        int balance = creditLedgerService.getBalanceByMember(memberId);
        Map<String, Object> response = new HashMap<>();
        response.put("memberId", memberId);
        response.put("creditBalance", balance);
        return ResponseEntity.ok(response);
    }
}
