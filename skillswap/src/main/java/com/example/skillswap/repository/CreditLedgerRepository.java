package com.example.skillswap.repository;

import com.example.skillswap.entity.CreditLedger;
import com.example.skillswap.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreditLedgerRepository extends JpaRepository<CreditLedger, Long> {

    // Find all ledger entries for a specific member
    List<CreditLedger> findByMember(Member member);
}
