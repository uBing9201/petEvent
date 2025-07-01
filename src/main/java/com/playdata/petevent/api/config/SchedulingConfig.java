package com.playdata.petevent.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 스케줄링 기능 활성화 설정.
 * 이 클래스가 존재하면 Spring에서 @Scheduled 어노테이션 기반 스케줄러 작동.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // 별도의 메서드 없이 EnableScheduling만 선언
}