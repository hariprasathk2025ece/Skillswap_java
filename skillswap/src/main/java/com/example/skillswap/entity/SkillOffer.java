package com.example.skillswap.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Entity
public class SkillOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Skill name is required")
    private String skillName;

    private String description;

    @Positive(message = "Hours available must be positive")
    private int hoursAvailable;

    // Many skill offers can belong to one provider (Member)
    @ManyToOne
    @JoinColumn(name = "provider_id")
    private Member provider;

    // Default constructor required by JPA
    public SkillOffer() {}

    public SkillOffer(String skillName, String description, int hoursAvailable, Member provider) {
        this.skillName = skillName;
        this.description = description;
        this.hoursAvailable = hoursAvailable;
        this.provider = provider;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getHoursAvailable() {
        return hoursAvailable;
    }

    public void setHoursAvailable(int hoursAvailable) {
        this.hoursAvailable = hoursAvailable;
    }

    public Member getProvider() {
        return provider;
    }

    public void setProvider(Member provider) {
        this.provider = provider;
    }
}
