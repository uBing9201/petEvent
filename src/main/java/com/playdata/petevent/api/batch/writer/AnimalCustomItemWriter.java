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
 * Spring Batch에서 데이터를 DB에 기록하는 역할을 하는 ItemWriter 구현체.

 * - AnimalApiItemReader로부터 넘어온 AnimalsEntity들을 DB에 저장 또는 업데이트.
 * - 이미 존재하는 유기번호(desertionNo)의 경우, 변경된 값만 업데이트.
 * - 처음 보는 유기번호는 신규 데이터로 저장.
 * - 유기번호를 Set에 저장해서 이후 삭제용 필터링에도 활용 가능.
 */
@Component
@RequiredArgsConstructor
public class AnimalCustomItemWriter implements ItemWriter<AnimalsEntity> {

    // Spring Data JPA를 통한 DB 접근용 Repository
    private final AnimalsRepository animalsRepository;

    // 이번 배치에서 API로 수집한 모든 유기번호를 모아놓는 Set
    // 이후 "DB에는 있는데, API에는 없는 데이터"를 삭제할 때 사용할 수 있음
    @Getter
    private final Set<String> desertionNoFromApi = new HashSet<>();

    /**
     * Chunk 단위로 데이터가 넘어오며, 각 동물 정보를 DB에 저장하거나 업데이트한다.
     * @param items 이번 배치 사이클에서 처리할 AnimalsEntity 리스트
     */
    @Override
    @Transactional  // 하나의 Chunk 안에서 모든 쓰기 작업이 트랜잭션으로 처리됨
    public void write(Chunk<? extends AnimalsEntity> items) {
        for (AnimalsEntity incoming : items) {

            // 유기번호를 Set에 저장 (삭제 대상 판별용)
            desertionNoFromApi.add(incoming.getDesertionNo());

            // DB에서 같은 유기번호가 이미 존재하는지 확인
            Optional<AnimalsEntity> optional = animalsRepository.findByDesertionNo(incoming.getDesertionNo());

            // 이미 존재하는 경우 → 데이터가 바뀌었는지 확인 후 업데이트
            if (optional.isPresent()) {
                AnimalsEntity existing = optional.get();

                // 비교해서 기존 데이터와 다른 경우만 업데이트
                if (isChanged(existing, incoming)) {
                    existing.updateIfChanged(incoming);  // 변경된 필드만 업데이트
                    animalsRepository.save(existing);    // DB 저장
                }

            } else {
                // DB에 존재하지 않는 유기번호 → 신규 데이터로 저장
                animalsRepository.save(incoming);
            }
        }
    }

    /**
     * 기존 DB의 동물 정보와 새로 들어온 정보 간에 변경 여부를 판단하는 메서드.
     * (비교 대상은 중요 필드만 선택적으로 지정)
     *
     * @param db 기존 DB에 저장된 AnimalsEntity
     * @param incoming 새로 들어온 AnimalsEntity
     * @return 변경된 필드가 하나라도 있으면 true
     */
    private boolean isChanged(AnimalsEntity db, AnimalsEntity incoming) {
        return !Objects.equals(db.getProcessState(), incoming.getProcessState()) ||
                !Objects.equals(db.getWeight(), incoming.getWeight()) ||
                !Objects.equals(db.getCareTel(), incoming.getCareTel());
        // 필요 시 비교 항목을 추가해 확장 가능
    }
}