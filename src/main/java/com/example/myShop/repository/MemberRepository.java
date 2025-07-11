package com.example.myShop.repository;

import com.example.myShop.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;



public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByEmail(String email);
}
