package com.example.api.service;

import com.example.api.repository.CouponJpaRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    @Test
    public void 여러명응모() throws InterruptedException {
        int threadCount = 1000;

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            // redis incr key:value
            long userId = i;
            executorService.submit(() -> {
                try {
                    couponService.apply(userId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        long count = couponJpaRepository.count();

        assertThat(count).isEqualTo(100);

        // 레이스 컨디션 문제가 발생한다.
        // 레이스 컨디션은 두개 이상의 스레드에서 공유자원에 access 하려 할때 발생하는 문제이다.

        // 예상 시나리오

        // coupon count: 99
        // T1: select count(*) from coupon; -> 99
        // T1: create coupon; -> 99 + 1

        // coupon count: 100
        // T2: select count(*) from coupon; -> 100
        // T2: failed create coupon

        // 현실

        // coupon count: 99
        // T1: select count(*) from coupon; -> 99
        // T2: select count(*) from coupon; -> 99

        // T1: create coupon; -> 99 + 1
        // T2: create coupon; -> 100 + 1

        // 이런 문제가 발생하여 쿠폰이 한도 이상으로 발급되는 문제가 발생한다

    }

}