package com.playdata.petevent.api.batch.writer;

import com.playdata.petevent.api.entity.AnimalsEntity;
import com.playdata.petevent.api.repository.AnimalsRepository;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Batch ItemWriter 구현체.
 * DB 저장소에 동물 데이터를 저장/갱신하는 역할.
 *
 * 특징:
 * - API에서 수집한 각 동물의 desertionNo를 Set에 저장(삭제 로직용).
 * - 기존 DB에 데이터가 존재하면 업데이트 조건 검사 후 변경된 부분만 수정 저장.
 * - 신규 데이터는 바로 저장.
 * - @Transactional로 쓰기 작업이 트랜잭션 단위로 묶임.
 */
@Component
@RequiredArgsConstructor
public class AnimalCustomItemWriter implements ItemWriter<AnimalsEntity> {

    private final AnimalsRepository animalsRepository;

    // API로부터 읽어온 유기번호를 저장하는 Set (삭제 대상 판별에 사용)
    @Getter
    private final Set<String> desertionNoFromApi = new HashSet<>();

    @Override
    @Transactional
    public void write(Chunk<? extends AnimalsEntity> items) {
        for (AnimalsEntity incoming : items) {
            desertionNoFromApi.add(incoming.getDesertionNo());

            Optional<AnimalsEntity> optional = animalsRepository.findByDesertionNo(incoming.getDesertionNo());

            if (optional.isPresent()) {
                AnimalsEntity existing = optional.get();

                // 데이터가 변경되었을 경우에만 업데이트 수행
                if (isChanged(existing, incoming)) {
                    existing.updateIfChanged(incoming);
                    animalsRepository.save(existing);
                }

            } else {
                // 신규 데이터 저장
                animalsRepository.save(incoming);
            }
        }
    }

    /**
     * 저장된 DB 데이터와 API 데이터 간 비교 후 변경점 존재 여부 반환
     * 주요 변경 가능 필드로 processState, weight, careTel 포함 (필요시 확장 가능)
     */
    private boolean isChanged(AnimalsEntity db, AnimalsEntity incoming) {
        return !Objects.equals(db.getProcessState(), incoming.getProcessState()) ||
                !Objects.equals(db.getWeight(), incoming.getWeight()) ||
                !Objects.equals(db.getCareTel(), incoming.getCareTel());
    }
}