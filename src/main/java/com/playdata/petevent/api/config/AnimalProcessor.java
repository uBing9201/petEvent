package com.playdata.petevent.api.config;

import com.playdata.petevent.api.entity.AnimalsEntity;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class AnimalProcessor implements ItemProcessor<AnimalsEntity, AnimalsEntity> {
    @Override
    public AnimalsEntity process(AnimalsEntity item) {
        // 전처리 필요 시 작성
        return item;
    }
}