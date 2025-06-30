package com.playdata.petevent.api.service.Impl;

import com.playdata.petevent.api.dto.ApiAnimalItem;
import com.playdata.petevent.api.dto.ApiResponse;
import com.playdata.petevent.api.entity.AnimalsEntity;
import com.playdata.petevent.api.repository.AnimalsRepository;
import com.playdata.petevent.api.service.AnimalsService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * AnimalsEntity 관련 비즈니스 로직 처리 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class AnimalsServiceImpl implements AnimalsService {

    private final AnimalsRepository animalsRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    // API 키 (실제 사용 시 config로 분리 권장)
    private final String SERVICE_KEY = "JSn0E7LvFMcdl+t/uNmxvKAfkGfvNVUlemWjY4O5+RNFksB7TRlw+XuaMe6Zz7Yt5QCYPl3G6Tc2t8jx6FUePg==";

    private final String BASE_URL = "https://apis.data.go.kr/1543061/abandonmentPublicService_v2/abandonmentPublic_v2";

    /**
     * 저장 또는 업데이트 처리 - desertionNo가 DB에 존재하지 않으면 새로 저장 - 존재하면 변경사항 있으면 업데이트, 없으면 기존 데이터 반환
     *
     * @param entity 저장할 동물 엔티티
     * @return 저장 또는 업데이트된 동물 엔티티
     */
    @Transactional
    @Override
    public AnimalsEntity saveOrUpdate(AnimalsEntity entity) {
        Optional<AnimalsEntity> existing = animalsRepository.findByDesertionNo(
                entity.getDesertionNo());

        if (existing.isPresent()) {
            AnimalsEntity savedEntity = existing.get();

            // 주요 필드 변경 여부 체크 (processState, weight 등)
            if (isEntityChanged(savedEntity, entity)) {
                savedEntity.updateIfChanged(entity);
                return animalsRepository.save(savedEntity);
            } else {
                return savedEntity;
            }
        } else {
            return animalsRepository.save(entity);
        }
    }

    /**
     * 주요 필드 변경 여부 체크 (null 처리 포함)
     */
    private boolean isEntityChanged(AnimalsEntity dbEntity, AnimalsEntity newEntity) {
        if (dbEntity.getProcessState() == null) {
            if (newEntity.getProcessState() != null)
                return true;
        } else if (!dbEntity.getProcessState().equals(newEntity.getProcessState())) {
            return true;
        }

        if (dbEntity.getWeight() == null) {
            if (newEntity.getWeight() != null)
                return true;
        } else if (!dbEntity.getWeight().equals(newEntity.getWeight())) {
            return true;
        }

        // 필요하면 더 체크 가능
        return false;
    }

    /**
     * DB에 존재하지만, 보호중이 아닌 동물 데이터 삭제 처리
     *
     * @param desertionNoList 삭제 대상 유기번호 목록
     */
    @Transactional
    @Override
    public void removeIfNotProtect(List<String> desertionNoList) {
        desertionNoList.forEach(desertionNo -> {
            animalsRepository.findByDesertionNo(desertionNo).ifPresent(entity -> {
                if (!"보호중".equals(entity.getProcessState())) {
                    animalsRepository.delete(entity);
                }
            });
        });
    }

    /**
     * API에서 주어진 state(protect/notice) 전체 데이터를 페이징으로 조회
     *
     * @param state 보호 상태 ("protect" or "notice")
     * @return 전체 데이터 리스트
     */
    private List<AnimalsEntity> fetchAllFromApi(String state) {
        List<AnimalsEntity> results = new ArrayList<>();

        int pageNo = 1;
        int numOfRows = 500;
        int totalCount = 0;

        do {
            // 요청 URL 생성
            String url = String.format(
                    "%s?serviceKey=%s&state=%s&numOfRows=%d&pageNo=%d&_type=json",
                    BASE_URL, SERVICE_KEY, state, numOfRows, pageNo);

            // API 호출
            ResponseEntity<ApiResponse> response = restTemplate.getForEntity(url,
                    ApiResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ApiResponse body = response.getBody();

                if (body.getResponse() != null
                        && body.getResponse().getBody() != null
                        && body.getResponse().getBody().getItems() != null
                        && body.getResponse().getBody().getItems().getItem() != null) {

                    List<ApiAnimalItem> items = body.getResponse().getBody().getItems().getItem();

                    for (ApiAnimalItem apiItem : items) {
                        AnimalsEntity entity = apiAnimalItemToEntity(apiItem);
                        results.add(entity);
                    }

                    totalCount = body.getResponse().getBody().getTotalCount();
                    pageNo++;
                } else {
                    break;
                }
            } else {
                break;
            }

        } while ((pageNo - 1) * numOfRows < totalCount);

        return results;
    }

    /**
     * API DTO -> AnimalsEntity 변환 메서드
     */
    private AnimalsEntity apiAnimalItemToEntity(ApiAnimalItem item) {
        AnimalsEntity.SexCode sexCode = null;
        if (item.getSexCd() != null) {
            try {
                sexCode = AnimalsEntity.SexCode.valueOf(item.getSexCd());
            } catch (IllegalArgumentException e) {
                sexCode = AnimalsEntity.SexCode.Q;
            }
        } else {
            sexCode = AnimalsEntity.SexCode.Q;
        }

        AnimalsEntity.NeuterYn neuterYn = null;
        if (item.getNeuterYn() != null) {
            try {
                neuterYn = AnimalsEntity.NeuterYn.valueOf(item.getNeuterYn());
            } catch (IllegalArgumentException e) {
                neuterYn = AnimalsEntity.NeuterYn.U;
            }
        } else {
            neuterYn = AnimalsEntity.NeuterYn.U;
        }

        return AnimalsEntity.builder()
                .desertionNo(item.getDesertionNo())
                .rfidCd(item.getRfidCd())
                .happenDt(item.getHappenDt())
                .happenPlace(item.getHappenPlace())
                .upKindNm(item.getUpKindNm())
                .kindNm(item.getKindNm())
                .colorCd(item.getColorCd())
                .age(item.getAge())
                .weight(item.getWeight())
                .noticeSdt(item.getNoticeSdt())
                .noticeEdt(item.getNoticeEdt())
                .popfile1(item.getPopfile())
                .popfile2(item.getPopfile2())
                .processState(item.getProcessState())
                .sexCd(sexCode)
                .neuterYn(neuterYn)
                .specialMark(item.getSpecialMark())
                .careNm(item.getCareNm())
                .careTel(item.getCareTel())
                .careAddr(item.getCareAddr())
                .careOwnerNm(item.getCareOwnerNm())
                .orgNm(item.getOrgNm())
                .etcBigo(item.getEtcBigo())
                .build();
    }
}