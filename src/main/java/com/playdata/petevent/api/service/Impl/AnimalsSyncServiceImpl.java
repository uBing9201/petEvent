package com.playdata.petevent.api.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playdata.petevent.api.entity.AnimalsEntity;
import com.playdata.petevent.api.repository.AnimalsRepository;
import com.playdata.petevent.api.service.AnimalsSyncService;
import com.playdata.petevent.api.service.AnimalsService;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnimalsSyncServiceImpl implements AnimalsSyncService {

    private final AnimalsService animalsService;
    private final AnimalsRepository animalsRepository;

    private final String[] states = {"protect", "notice"};

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String BASE_URL =
            "https://apis.data.go.kr/1543061/abandonmentPublicService_v2/abandonmentPublic_v2" +
                    "?serviceKey=JSn0E7LvFMcdl%2Bt%2FuNmxvKAfkGfvNVUlemWjY4O5%2BRNFksB7TRlw%2BXuaMe6Zz7Yt5QCYPl3G6Tc2t8jx6FUePg%3D%3D" +
                    "&_type=json&numOfRows=500";

    @Override
    @Scheduled(fixedRate = 3600000) // 1시간 마다
    public void syncAbandonedAnimals() {
        List<String> allDesertionNoFromAPI = new ArrayList<>();

        for (String state : states) {
            int pageNo = 1;
            int totalCount;

            do {
                String url = BASE_URL + "&state=" + state + "&pageNo=" + pageNo;
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(new URI(url))
                            .GET()
                            .build();

                    HttpResponse<String> response = HttpClient.newHttpClient()
                            .send(request, HttpResponse.BodyHandlers.ofString());

                    JsonNode root = objectMapper.readTree(response.body());
                    JsonNode body = root.path("response").path("body");
                    totalCount = body.path("totalCount").asInt();

                    JsonNode items = body.path("items").path("item");

                    if (items.isArray()) {
                        for (JsonNode item : items) {
                            AnimalsEntity entity = parseToEntity(item);
                            allDesertionNoFromAPI.add(entity.getDesertionNo());
                            animalsService.saveOrUpdate(entity);
                        }
                    } else {
                        AnimalsEntity entity = parseToEntity(items);
                        allDesertionNoFromAPI.add(entity.getDesertionNo());
                        animalsService.saveOrUpdate(entity);
                    }

                    pageNo++;
                } catch (Exception e) {
                    log.error("API 동기화 중 오류 발생", e);
                    break;
                }
            } while ((pageNo - 1) * 500 < totalCount);
        }

        // 삭제 대상 처리
        List<AnimalsEntity> allInDB = animalsRepository.findAll();
        List<String> toCheckForDelete = new ArrayList<>();
        for (AnimalsEntity entity : allInDB) {
            if (!allDesertionNoFromAPI.contains(entity.getDesertionNo())) {
                toCheckForDelete.add(entity.getDesertionNo());
            }
        }
        animalsService.removeIfNotProtect(toCheckForDelete);
    }

    private AnimalsEntity parseToEntity(JsonNode item) {
        return AnimalsEntity.builder()
                .desertionNo(item.path("desertionNo").asText())
                .rfidCd(item.path("rfidCd").asText(null))
                .happenDt(item.path("happenDt").asText(null))
                .happenPlace(item.path("happenPlace").asText(null))
                .upKindNm(item.path("upKindNm").asText(null))
                .kindNm(item.path("kindNm").asText(null))
                .colorCd(item.path("colorCd").asText(null))
                .age(item.path("age").asText(null))
                .weight(item.path("weight").asText(null))
                .noticeSdt(item.path("noticeSdt").asText(null))
                .noticeEdt(item.path("noticeEdt").asText(null))
                .popfile1(item.path("popfile1").asText(null))
                .popfile2(item.path("popfile2").asText(null))
                .processState(item.path("processState").asText(null))
                .sexCd(AnimalsEntity.SexCode.valueOf(item.path("sexCd").asText("Q")))
                .neuterYn(AnimalsEntity.NeuterYn.valueOf(item.path("neuterYn").asText("U")))
                .specialMark(item.path("specialMark").asText(null))
                .careNm(item.path("careNm").asText(null))
                .careTel(item.path("careTel").asText(null))
                .careAddr(item.path("careAddr").asText(null))
                .careOwnerNm(item.path("careOwnerNm").asText(null))
                .orgNm(item.path("orgNm").asText(null))
                .etcBigo(item.path("etcBigo").asText(null))
                .build();
    }
}