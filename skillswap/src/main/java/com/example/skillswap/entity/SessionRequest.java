package com.example.skillswap.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;

@Entity
public class SessionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which skill is being requested
    @ManyToOne
    @JoinColumn(name = "skill_offer_id")
    private SkillOffer skillOffer;

    // Who is requesting the skill
    @ManyToOne
    @JoinColumn(name = "requester_id")
    private Member requester;

    @Positive(message = "Hours requested must be positive")
    private int hoursRequested;

    // Status: PENDING, CONFIRMED, COMPLETED, REJECTED
    private String status;

    // Default constructor required by JPA
    public SessionRequest() {}

    public SessionRequest(SkillOffer skillOffer, Member requester, int hoursRequested) {
        this.skillOffer = skillOffer;
        this.requester = requester;
        this.hoursRequested = hoursRequested;
        this.status = "PENDING"; // always starts as PENDING
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public SkillOffer getSkillOffer() {
        return skillOffer;
    }

    public void setSkillOffer(SkillOffer skillOffer) {
        this.skillOffer = skillOffer;
    }

    public Member getRequester() {
        return requester;
    }

    public void setRequester(Member requester) {
        this.requester = requester;
    }

    public int getHoursRequested() {
        return hoursRequested;
    }

    public void setHoursRequested(int hoursRequested) {
        this.hoursRequested = hoursRequested;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
