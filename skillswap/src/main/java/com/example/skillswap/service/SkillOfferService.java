package com.example.skillswap.service;

import com.example.skillswap.entity.Member;
import com.example.skillswap.entity.SessionRequest;
import com.example.skillswap.entity.SkillOffer;
import com.example.skillswap.exception.ResourceNotFoundException;
import com.example.skillswap.repository.MemberRepository;
import com.example.skillswap.repository.SessionRequestRepository;
import com.example.skillswap.repository.SkillOfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SkillOfferService {

    private final SkillOfferRepository skillOfferRepository;
    private final MemberRepository memberRepository;
    private final SessionRequestRepository sessionRequestRepository;

    public SkillOfferService(
            SkillOfferRepository skillOfferRepository,
            MemberRepository memberRepository,
            SessionRequestRepository sessionRequestRepository) {
        this.skillOfferRepository = skillOfferRepository;
        this.memberRepository = memberRepository;
        this.sessionRequestRepository = sessionRequestRepository;
    }

    // Create a skill offer — we need to look up the provider member first
    public SkillOffer createSkillOffer(String skillName, String description, int hoursAvailable, Long providerId) {
        Member provider = memberRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider member not found with id: " + providerId));

        SkillOffer offer = new SkillOffer(skillName, description, hoursAvailable, provider);
        return skillOfferRepository.save(offer);
    }

    // Get all skill offers
    public List<SkillOffer> getAllSkillOffers() {
        return skillOfferRepository.findAll();
    }

    // Get one skill offer by ID
    public SkillOffer getSkillOfferById(Long id) {
        return skillOfferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill offer not found with id: " + id));
    }

    // Update a skill offer
    public SkillOffer updateSkillOffer(Long id, String skillName, String description, int hoursAvailable, Long providerId) {
        SkillOffer existing = getSkillOfferById(id);

        Member provider = memberRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider member not found with id: " + providerId));

        existing.setSkillName(skillName);
        existing.setDescription(description);
        existing.setHoursAvailable(hoursAvailable);
        existing.setProvider(provider);

        return skillOfferRepository.save(existing);
    }

    // Delete a skill offer (clean up associated session requests)
    @Transactional
    public void deleteSkillOffer(Long id) {
        SkillOffer offer = getSkillOfferById(id);

        List<SessionRequest> sessions = sessionRequestRepository.findAll();
        for (SessionRequest req : sessions) {
            if (req.getSkillOffer() != null && req.getSkillOffer().getId().equals(id)) {
                sessionRequestRepository.delete(req);
            }
        }

        skillOfferRepository.delete(offer);
    }
}
