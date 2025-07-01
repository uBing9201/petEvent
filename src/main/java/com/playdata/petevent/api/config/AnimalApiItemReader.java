package com.playdata.petevent.api.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playdata.petevent.api.entity.AnimalsEntity;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class AnimalApiItemReader implements ItemReader<AnimalsEntity> {

    private final Iterator<AnimalsEntity> dataIterator;

    public AnimalApiItemReader() {
        List<AnimalsEntity> results = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        List<String> states = List.of("protect", "notice");

        try {
            for (String state : states) {
                int pageNo = 1;
                int numOfRows = 500;
                int totalCount;

                do {
                    String url = String.format("https://apis.data.go.kr/1543061/abandonmentPublicService_v2/abandonmentPublic_v2" +
                                    "?serviceKey=JSn0E7LvFMcdl%%2Bt%%2FuNmxvKAfkGfvNVUlemWjY4O5%%2BRNFksB7TRlw%%2BXuaMe6Zz7Yt5QCYPl3G6Tc2t8jx6FUePg%%3D%%3D" +
                                    "&_type=json&numOfRows=%d&pageNo=%d&state=%s",
                            numOfRows, pageNo, state);

                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .GET()
                            .build();

                    HttpResponse<String> response = HttpClient.newHttpClient()
                            .send(request, HttpResponse.BodyHandlers.ofString());

                    JsonNode body = mapper.readTree(response.body())
                            .path("response").path("body");

                    JsonNode items = body.path("items").path("item");
                    totalCount = body.path("totalCount").asInt();

                    if (items.isArray()) {
                        for (JsonNode item : items) {
                            results.add(parseToEntity(item));
                        }
                    } else if (items.isObject()) {
                        results.add(parseToEntity(items));
                    }

                    pageNo++;
                } while ((pageNo - 1) * numOfRows < totalCount);
            }
        } catch (Exception e) {
            throw new RuntimeException("API 호출 실패", e);
        }

        this.dataIterator = results.iterator();
    }

    @Override
    public AnimalsEntity read() {
        return dataIterator.hasNext() ? dataIterator.next() : null;
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
                .sexCd(SafeEnumParser.parseSexCode(item.path("sexCd").asText("Q")))
                .neuterYn(SafeEnumParser.parseNeuterYn(item.path("neuterYn").asText("U")))
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