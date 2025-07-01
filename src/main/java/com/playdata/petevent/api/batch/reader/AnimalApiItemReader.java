package com.playdata.petevent.api.batch.reader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playdata.petevent.api.util.SafeEnumParser;
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

/**
 * Spring Batch의 ItemReader 구현체.
 * 외부 공공 API(유기동물 보호센터 데이터)를 호출해 데이터를 읽어옴.
 *
 * 특징:
 * - API가 페이징 방식이므로 모든 페이지를 순회하며 데이터를 수집.
 * - 수집한 데이터는 메모리에 저장해 Iterator로 순차 제공.
 * - StepScope를 사용해 Job 파라미터 주입 또는 스텝 실행 시 인스턴스 재생성 가능.
 */
@Component
@StepScope
public class AnimalApiItemReader implements ItemReader<AnimalsEntity> {

    // API로부터 읽어온 동물 데이터 리스트를 순회하는 Iterator
    private final Iterator<AnimalsEntity> dataIterator;

    public AnimalApiItemReader() {
        List<AnimalsEntity> results = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        // API에서 보호 상태 필터 (protect: 보호중, notice: 공고중)
        List<String> states = List.of("protect", "notice");

        try {
            for (String state : states) {
                int pageNo = 1;
                int numOfRows = 500;
                int totalCount;
                String serviceKey = "JSn0E7LvFMcdl%2Bt%2FuNmxvKAfkGfvNVUlemWjY4O5%2BRNFksB7TRlw%2BXuaMe6Zz7Yt5QCYPl3G6Tc2t8jx6FUePg%3D%3D";

                do {
                    // API 호출용 URL 조립
                    String url = String.format(
                            "https://apis.data.go.kr/1543061/abandonmentPublicService_v2/abandonmentPublic_v2?serviceKey=%s&_type=json&numOfRows=%d&pageNo=%d&state=%s",
                            serviceKey, numOfRows, pageNo, state);

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

        // 수집한 데이터를 Iterator로 설정하여 read() 호출 시 순차 제공
        this.dataIterator = results.iterator();
    }

    /**
     * 배치 Step이 한 건씩 데이터 요청 시 호출됨.
     * Iterator에서 다음 요소를 반환하고 없으면 null 반환하여 Step 종료 신호.
     */
    @Override
    public AnimalsEntity read() {
        return dataIterator.hasNext() ? dataIterator.next() : null;
    }

    /**
     * JsonNode를 AnimalsEntity 객체로 변환.
     * API 응답 필드와 엔티티 필드 매핑 처리 및 enum 안전 변환 적용.
     */
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