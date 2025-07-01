package com.playdata.petevent.api.batch.processor;

import com.playdata.petevent.api.entity.AnimalsEntity;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * Spring Batch의 ItemProcessor 인터페이스 구현체.
 * 배치 처리 시 읽어온 데이터를 가공하거나 필터링하는 역할을 함.

 * 현재 구현은 입력받은 데이터를 그대로 반환하는 단순 패스스루(pass-through) 처리.
 * 추후 데이터 변환, 필터링 로직 추가 가능.
 */
@Component
public class AnimalProcessor implements ItemProcessor<AnimalsEntity, AnimalsEntity> {

    @Override
    public AnimalsEntity process(AnimalsEntity item) {
        // 변형 없이 그대로 반환
        return item;
    }
}
