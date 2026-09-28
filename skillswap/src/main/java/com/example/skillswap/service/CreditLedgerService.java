package com.example.skillswap.service;

import com.example.skillswap.entity.CreditLedger;
import com.example.skillswap.entity.Member;
import com.example.skillswap.exception.ResourceNotFoundException;
import com.example.skillswap.repository.CreditLedgerRepository;
import com.example.skillswap.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreditLedgerService {

    private final CreditLedgerRepository creditLedgerRepository;
    private final MemberRepository memberRepository;

    public CreditLedgerService(CreditLedgerRepository creditLedgerRepository, MemberRepository memberRepository) {
        this.creditLedgerRepository = creditLedgerRepository;
        this.memberRepository = memberRepository;
    }

    // Get all ledger entries for a member
    public List<CreditLedger> getLedgerByMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));

        return creditLedgerRepository.findByMember(member);
    }

    // Get current balance for a member
    public int getBalanceByMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + memberId));

        return member.getCreditBalance();
    }
}
