package com.example.myShop.repository;

import com.example.myShop.entity.PaymentTemp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentTempRepository extends JpaRepository<PaymentTemp, String> {
    Optional<PaymentTemp> findByTid(String tid);
    Optional<PaymentTemp> findTopByEmailOrderByCreatedAtDesc(String email);
}
