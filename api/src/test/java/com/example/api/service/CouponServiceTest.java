package com.example.api.service;

import com.example.api.repository.CouponJpaRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CouponServiceTest {

    @Autowired
    CouponService couponService;

    @Autowired
    CouponJpaRepository couponJpaRepository;

    @Test
    public void 한번만응모() {

        couponService.apply(1L);

        long count = couponJpaRepository.count();

        assertThat(count).isEqualTo(1);

    }

}