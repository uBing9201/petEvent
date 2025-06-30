package com.playdata.petevent.api.controller;

import com.playdata.petevent.api.service.AnimalsSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/animals")
public class AnimalsController {

    private final AnimalsSyncService animalsSyncService;

    // 수동 동기화 트리거용 엔드포인트
    @GetMapping("/sync")
    public String syncData() {
        animalsSyncService.syncAbandonedAnimals();
        return "동기화 완료";
    }
}