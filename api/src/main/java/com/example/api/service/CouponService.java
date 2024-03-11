package com.example.api.service;

import com.example.api.domain.Coupon;
import com.example.api.repository.CouponCountRedisRepository;
import com.example.api.repository.CouponJpaRepository;
import org.springframework.stereotype.Service;

@Service
public class CouponService {

    private final CouponJpaRepository couponJpaRepository;
    private final CouponCountRedisRepository couponCountRedisRepository;

    public CouponService(CouponJpaRepository couponJpaRepository, CouponCountRedisRepository couponCountRedisRepository) {
        this.couponJpaRepository = couponJpaRepository;
        this.couponCountRedisRepository = couponCountRedisRepository;
    }

    public void apply(Long userId) {

        Long count = couponCountRedisRepository.increment();

        if (count > 100) {
            return;
        }

        couponJpaRepository.save(new Coupon(userId));

    }

}
