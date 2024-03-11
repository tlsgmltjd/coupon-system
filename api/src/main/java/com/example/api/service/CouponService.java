package com.example.api.service;

import com.example.api.domain.Coupon;
import com.example.api.repository.CouponJpaRepository;
import org.springframework.stereotype.Service;

@Service
public class CouponService {

    private final CouponJpaRepository couponJpaRepository;

    public CouponService(CouponJpaRepository couponJpaRepository) {
        this.couponJpaRepository = couponJpaRepository;
    }

    public void apply(Long userId) {

        long count = couponJpaRepository.count();

        if (count > 100) {
            return;
        }

        couponJpaRepository.save(new Coupon(userId));

    }

}
