package com.example.api.service;

import com.example.api.producer.CouponCreateProducer;
import com.example.api.repository.AppliedUserRepository;
import com.example.api.repository.CouponCountRedisRepository;
import com.example.api.repository.CouponJpaRepository;
import org.springframework.stereotype.Service;

@Service
public class CouponService {

    private final CouponJpaRepository couponJpaRepository;
    private final CouponCountRedisRepository couponCountRedisRepository;
    private final CouponCreateProducer couponCreateProducer;
    private final AppliedUserRepository appliedUserRepository;

    public CouponService(CouponJpaRepository couponJpaRepository, CouponCountRedisRepository couponCountRedisRepository, CouponCreateProducer couponCreateProducer, AppliedUserRepository appliedUserRepository) {
        this.couponJpaRepository = couponJpaRepository;
        this.couponCountRedisRepository = couponCountRedisRepository;
        this.couponCreateProducer = couponCreateProducer;
        this.appliedUserRepository = appliedUserRepository;
    }

    public void apply(Long userId) {

        Long apply = appliedUserRepository.add(userId);

        if (apply != 1) {
            return;
        }

        Long count = couponCountRedisRepository.increment();

        if (count > 100) {
            return;
        }

        couponCreateProducer.create(userId);
    }

}
