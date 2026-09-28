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

import java.util.List;

@Service
public class SessionRequestService {

    private final SessionRequestRepository sessionRequestRepository;
    private final MemberRepository memberRepository;
    private final SkillOfferRepository skillOfferRepository;
    private final CreditLedgerRepository creditLedgerRepository;

    public SessionRequestService(
            SessionRequestRepository sessionRequestRepository,
            MemberRepository memberRepository,
            SkillOfferRepository skillOfferRepository,
            CreditLedgerRepository creditLedgerRepository) {
        this.sessionRequestRepository = sessionRequestRepository;
        this.memberRepository = memberRepository;
        this.skillOfferRepository = skillOfferRepository;
        this.creditLedgerRepository = creditLedgerRepository;
    }

    // Create a session request
    // Business Rule: requester must have enough credits
    public SessionRequest createSessionRequest(Long skillOfferId, Long requesterId, int hoursRequested) {

        // Step 1: Find the requester
        Member requester = memberRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Requester not found with id: " + requesterId));

        // Step 2: Find the skill offer
        SkillOffer skillOffer = skillOfferRepository.findById(skillOfferId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill offer not found with id: " + skillOfferId));

        // Step 3: Check if requester has enough credits
        if (requester.getCreditBalance() < hoursRequested) {
            throw new IllegalArgumentException("Insufficient credit balance");
        }

        // Step 4: Create the request with PENDING status — no credit transfer yet
        SessionRequest request = new SessionRequest(skillOffer, requester, hoursRequested);
        return sessionRequestRepository.save(request);
    }

    // Get all session requests
    public List<SessionRequest> getAllSessionRequests() {
        return sessionRequestRepository.findAll();
    }

    // Get one session request by ID
    public SessionRequest getSessionRequestById(Long id) {
        return sessionRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session request not found with id: " + id));
    }

    // Provider confirms the session
    // Status changes from PENDING to CONFIRMED
    public SessionRequest confirmSession(Long id) {
        SessionRequest request = getSessionRequestById(id);

        if (!request.getStatus().equals("PENDING")) {
            throw new IllegalArgumentException("Only PENDING sessions can be confirmed");
        }

        request.setStatus("CONFIRMED");
        return sessionRequestRepository.save(request);
    }

    // Provider completes the session — this is where credits are actually transferred
    @Transactional
    public SessionRequest completeSession(Long id, int actualHours) {

        // Step 1: Get the session
        SessionRequest request = getSessionRequestById(id);

        // Step 2: Only CONFIRMED sessions can be completed
        if (!request.getStatus().equals("CONFIRMED")) {
            throw new IllegalArgumentException("Only CONFIRMED sessions can be completed. Current status: " + request.getStatus());
        }

        // Step 3: Actual hours must be positive
        if (actualHours <= 0) {
            throw new IllegalArgumentException("Actual hours must be greater than zero");
        }

        Member requester = request.getRequester();
        Member provider = request.getSkillOffer().getProvider();

        // Step 4: Check requester still has enough credits
        if (requester.getCreditBalance() < actualHours) {
            throw new IllegalArgumentException("Requester does not have enough credits to complete the session");
        }

        // Step 5: Deduct credits from requester
        requester.setCreditBalance(requester.getCreditBalance() - actualHours);
        memberRepository.save(requester);

        // Step 6: Add credits to provider
        provider.setCreditBalance(provider.getCreditBalance() + actualHours);
        memberRepository.save(provider);

        // Step 7: Record DEBIT for requester
        CreditLedger requesterLedger = new CreditLedger(
                requester,
                actualHours,
                "DEBIT",
                "Payment for session: " + request.getSkillOffer().getSkillName()
        );
        creditLedgerRepository.save(requesterLedger);

        // Step 8: Record CREDIT for provider
        CreditLedger providerLedger = new CreditLedger(
                provider,
                actualHours,
                "CREDIT",
                "Earned for teaching: " + request.getSkillOffer().getSkillName()
        );
        creditLedgerRepository.save(providerLedger);

        // Step 9: Mark session as COMPLETED
        request.setStatus("COMPLETED");
        return sessionRequestRepository.save(request);
    }
}
