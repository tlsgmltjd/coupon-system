package com.example.consumer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.consumer.domain.Coupon;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {
}
