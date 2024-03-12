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

        Thread.sleep(5000);

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

        // redis를 사용하여 해당 문제를 해결할 수 있다

        // redis의 incr를 사용하여 쿠폰을 발급하기 전 값을 1씩 올리며 쿠폰의 제한 개수를 유지한다
        // 이렇게하면 값을 올리는 작업을 수행중이라면 다른 스레드에서 값을 올리는 작업을 실행해도 기다린 후 작업을 시작하게 된다

        // T1: 10:00 : 10:02 incr coupon_count -> 99 + 1
        // T1: 10:03 create coupon

        // T2: 10:01 wait...
        // T2: 10:02: wait...
        // T2: 10:03 incr coupon_count -> 100 + 1
        // failed create coupon

        // 이렇게 스레드에서 언제나 최신값을 가져갈 수 있기 때문에 레이스 컨디션을 해결할 수 있다.

        // ===
        // 하지만 redis를 사용해서 레이스 컨디션을 방지해도 많은 트래픽이 발생한다면 문제가 생긴다.
        // 현재 방식은 redis에서 쿠폰 발급 개수를 가져온 후 발급이 가능하다면 rdb에 저장하는 방식이다.

        // 현재 rdb가 1분에 100개의 insert 쿼리가 가능하다고 가정한다

        // 10:00 쿠폰 생성 10000개 요청
        // 10:01 주문생성 요청
        // 10:02 회원가입 요청

        // 이렇게 된다면 쿠폰 1만개의 요청이 한번에 왔기 때문에 뒤에 들어온 요청인 주문 생성과 회원가입은 100분이나 지난 후에 작업이 실행된다.
        // 하지만 대부분의 dbms에서는 타임아웃이 적용되어있어서 주문, 회원가입, 쿠폰의 일부분도 생성되지 않는 치명적인 오류가 발생한다.
        // 그리고 짧은 시간에 많은 요청이 들어오게 된다면 db 서버의 리소스를 많이 사용하게 되어 부하를 유발하고 전체 서비스 장애까지 이어지게 할 수 있다.

        // kafka
        // 분산 이벤트 스트리밍 플랫폼
        // Producer -> Topic <- Consumer
        // Topic: Queue랑 비슷하다
        // Producer: Topic에 데이터를 삽입한다.
        // Consumer: Topic에 삽입된 데이터를 가져간다.
        // 카프카는 프로듀서, 컨슈머 즉 목적지까지 데이터를 실시간으로 스트리밍해주는 서비스이다.

    }


    @Test
    public void 한명당_한개의쿠폰만_응모() throws InterruptedException {
        int threadCount = 1000;

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            long userId = 1L;
            executorService.submit(() -> {
                try {
                    couponService.apply(userId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Thread.sleep(5000);

        long count = couponJpaRepository.count();

        assertThat(count).isEqualTo(1);

    }

}