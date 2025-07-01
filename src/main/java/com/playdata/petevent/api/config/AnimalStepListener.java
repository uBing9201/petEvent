package com.playdata.petevent.api.config;

import com.playdata.petevent.api.batch.writer.AnimalCustomItemWriter;
import com.playdata.petevent.api.entity.AnimalsEntity;
import com.playdata.petevent.api.repository.AnimalsRepository;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

/**
 * Spring Batch StepExecutionListener 구현체.
 * Step 종료 후 호출되어 DB 데이터 정합성 검사 및 삭제 처리 수행.
 *
 * 역할:
 * - API로부터 수집한 desertionNo 리스트를 기준으로 DB에 남아있는 데이터 중
 *   API에 존재하지 않고 분양중이 아닌 동물 데이터 삭제.
 *
 * 삭제 조건: API에 없고(processState != '분양중')인 경우만 삭제하여
 * 분양중인 데이터는 삭제하지 않음.
 */
@Component
@RequiredArgsConstructor
public class AnimalStepListener implements StepExecutionListener {

    private final AnimalsRepository animalsRepository;
    private final AnimalCustomItemWriter itemWriter;

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        Set<String> desertionNoFromApi = itemWriter.getDesertionNoFromApi();
        List<AnimalsEntity> allInDb = animalsRepository.findAll();

        for (AnimalsEntity entity : allInDb) {
            boolean notInApi = !desertionNoFromApi.contains(entity.getDesertionNo());
            boolean notProtecting = !"분양중".equals(entity.getProcessState());

            // API에 없고 분양중이 아니면 DB에서 삭제
            if (notInApi && notProtecting) {
                animalsRepository.delete(entity);
            }
        }
        return null; // 특별한 종료 상태 없음
    }
}