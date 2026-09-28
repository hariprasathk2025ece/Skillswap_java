package com.example.skillswap.controller;

import com.example.skillswap.entity.SessionRequest;
import com.example.skillswap.service.SessionRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/session-requests")
public class SessionRequestController {

    private final SessionRequestService sessionRequestService;

    public SessionRequestController(SessionRequestService sessionRequestService) {
        this.sessionRequestService = sessionRequestService;
    }

    // POST /api/session-requests — Create a session request
    @PostMapping
    public ResponseEntity<SessionRequest> createSessionRequest(@RequestBody Map<String, Object> body) {
        Long skillOfferId = Long.valueOf(body.get("skillOfferId").toString());
        Long requesterId = Long.valueOf(body.get("requesterId").toString());
        int hoursRequested = (Integer) body.get("hoursRequested");

        if (hoursRequested <= 0) {
            throw new IllegalArgumentException("Hours requested must be positive");
        }

        SessionRequest created = sessionRequestService.createSessionRequest(skillOfferId, requesterId, hoursRequested);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // GET /api/session-requests — Get all session requests
    @GetMapping
    public ResponseEntity<List<SessionRequest>> getAllSessionRequests() {
        return ResponseEntity.ok(sessionRequestService.getAllSessionRequests());
    }

    // GET /api/session-requests/{id} — Get one session request
    @GetMapping("/{id}")
    public ResponseEntity<SessionRequest> getSessionRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(sessionRequestService.getSessionRequestById(id));
    }

    // PUT /api/session-requests/{id}/confirm — Provider confirms the session
    @PutMapping("/{id}/confirm")
    public ResponseEntity<SessionRequest> confirmSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionRequestService.confirmSession(id));
    }

    // PUT /api/session-requests/{id}/complete — Provider completes the session
    // This is where credits are transferred
    @PutMapping("/{id}/complete")
    public ResponseEntity<SessionRequest> completeSession(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        int actualHours = (Integer) body.get("actualHours");
        return ResponseEntity.ok(sessionRequestService.completeSession(id, actualHours));
    }
}
