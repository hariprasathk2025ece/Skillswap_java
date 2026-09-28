package com.example.skillswap.repository;

import com.example.skillswap.entity.SessionRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRequestRepository extends JpaRepository<SessionRequest, Long> {
}
