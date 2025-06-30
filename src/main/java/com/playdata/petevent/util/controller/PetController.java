package com.playdata.petevent.util.controller;

import com.playdata.petevent.util.crawler.NaverPetEventCrawler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PetController {

    private final NaverPetEventCrawler crawler;

    /**
     * 수동으로 크롤러를 실행하는 엔드포인트
     * GET /api/crawler/run
     */
    @GetMapping("/run")
    public String runCrawler() {
        try {
            crawler.crawl();
            return "크롤링이 완료되었습니다.";
        } catch (Exception e) {
            e.printStackTrace();
            return "크롤링 도중 오류가 발생했습니다.";
        }
    }
}
