package com.example.skillswap.service;

import com.example.skillswap.entity.CreditLedger;
import com.example.skillswap.entity.Member;
import com.example.skillswap.entity.SessionRequest;
import com.example.skillswap.entity.SkillOffer;
import com.example.skillswap.exception.ResourceNotFoundException;
import com.example.skillswap.repository.CreditLedgerRepository;
import com.example.skillswap.repository.MemberRepository;
import com.example.skillswap.repository.SessionRequestRepository;
import com.example.skillswap.repository.SkillOfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final SkillOfferRepository skillOfferRepository;
    private final SessionRequestRepository sessionRequestRepository;
    private final CreditLedgerRepository creditLedgerRepository;

    // Constructor injection
    public MemberService(
            MemberRepository memberRepository,
            SkillOfferRepository skillOfferRepository,
            SessionRequestRepository sessionRequestRepository,
            CreditLedgerRepository creditLedgerRepository) {
        this.memberRepository = memberRepository;
        this.skillOfferRepository = skillOfferRepository;
        this.sessionRequestRepository = sessionRequestRepository;
        this.creditLedgerRepository = creditLedgerRepository;
    }

    // Create a new member
    public Member createMember(Member member) {
        return memberRepository.save(member);
    }

    // Get all members
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    // Get one member by ID
    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
    }

    // Update member details
    public Member updateMember(Long id, Member updatedMember) {
        Member existing = getMemberById(id);
        existing.setName(updatedMember.getName());
        existing.setEmail(updatedMember.getEmail());
        existing.setCreditBalance(updatedMember.getCreditBalance());
        return memberRepository.save(existing);
    }

    // Delete a member (clean up dependent records safely)
    @Transactional
    public void deleteMember(Long id) {
        Member member = getMemberById(id);

        // 1. Delete all session requests where member is requester or provider of the skill
        List<SessionRequest> allSessions = sessionRequestRepository.findAll();
        for (SessionRequest req : allSessions) {
            boolean isRequester = req.getRequester() != null && req.getRequester().getId().equals(id);
            boolean isProvider = req.getSkillOffer() != null && req.getSkillOffer().getProvider() != null && req.getSkillOffer().getProvider().getId().equals(id);
            if (isRequester || isProvider) {
                sessionRequestRepository.delete(req);
            }
        }

        // 2. Delete all skill offers posted by this member
        List<SkillOffer> allSkills = skillOfferRepository.findAll();
        for (SkillOffer skill : allSkills) {
            if (skill.getProvider() != null && skill.getProvider().getId().equals(id)) {
                skillOfferRepository.delete(skill);
            }
        }

        // 3. Delete all ledger transactions for this member
        List<CreditLedger> ledgers = creditLedgerRepository.findByMember(member);
        creditLedgerRepository.deleteAll(ledgers);

        // 4. Delete the member
        memberRepository.delete(member);
    }

    // Get just the balance info for a member
    public Map<String, Object> getBalance(Long id) {
        Member member = getMemberById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("memberId", member.getId());
        response.put("name", member.getName());
        response.put("creditBalance", member.getCreditBalance());
        return response;
    }
}
