package com.example.skillswap.repository;

import com.example.skillswap.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository gives us save(), findById(), findAll(), delete() etc. for free
public interface MemberRepository extends JpaRepository<Member, Long> {
}
