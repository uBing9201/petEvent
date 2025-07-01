package com.playdata.petevent.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfig {
    // 이 클래스가 존재하면 Spring이 스케줄링을 인식합니다.
}
