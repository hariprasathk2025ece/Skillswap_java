package com.example.skillswap.controller;

import com.example.skillswap.entity.SkillOffer;
import com.example.skillswap.service.SkillOfferService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/skills")
public class SkillOfferController {

    private final SkillOfferService skillOfferService;

    public SkillOfferController(SkillOfferService skillOfferService) {
        this.skillOfferService = skillOfferService;
    }

    // POST /api/skills — Create a skill offer
    // The request body has flat fields (skillName, description, hoursAvailable, providerId)
    @PostMapping
    public ResponseEntity<SkillOffer> createSkillOffer(@RequestBody Map<String, Object> body) {
        String skillName = (String) body.get("skillName");
        String description = (String) body.get("description");
        int hoursAvailable = (Integer) body.get("hoursAvailable");
        Long providerId = Long.valueOf(body.get("providerId").toString());

        if (skillName == null || skillName.isBlank()) {
            throw new IllegalArgumentException("Skill name is required");
        }
        if (hoursAvailable <= 0) {
            throw new IllegalArgumentException("Hours available must be positive");
        }

        SkillOffer created = skillOfferService.createSkillOffer(skillName, description, hoursAvailable, providerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // GET /api/skills — Get all skill offers
    @GetMapping
    public ResponseEntity<List<SkillOffer>> getAllSkillOffers() {
        return ResponseEntity.ok(skillOfferService.getAllSkillOffers());
    }

    // GET /api/skills/{id} — Get one skill offer
    @GetMapping("/{id}")
    public ResponseEntity<SkillOffer> getSkillOfferById(@PathVariable Long id) {
        return ResponseEntity.ok(skillOfferService.getSkillOfferById(id));
    }

    // PUT /api/skills/{id} — Update skill offer
    @PutMapping("/{id}")
    public ResponseEntity<SkillOffer> updateSkillOffer(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String skillName = (String) body.get("skillName");
        String description = (String) body.get("description");
        int hoursAvailable = (Integer) body.get("hoursAvailable");
        Long providerId = Long.valueOf(body.get("providerId").toString());

        SkillOffer updated = skillOfferService.updateSkillOffer(id, skillName, description, hoursAvailable, providerId);
        return ResponseEntity.ok(updated);
    }

    // DELETE /api/skills/{id} — Delete skill offer
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSkillOffer(@PathVariable Long id) {
        skillOfferService.deleteSkillOffer(id);
        return ResponseEntity.noContent().build();
    }
}
