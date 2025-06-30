package com.playdata.petevent.util.service.Impl;

import com.playdata.petevent.util.entity.PetEvent;
import com.playdata.petevent.util.repository.PetRepository;
import com.playdata.petevent.util.service.PetService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PetServiceImpl implements PetService {

    private final PetRepository repository;

    /**
     * DB에 이벤트 리스트를 저장한다.
     * 저장 전, 동일 해시(hash)를 가진 이벤트가 이미 존재하는지 확인하여 중복 저장을 방지한다.
     *
     * @param events 저장할 PetEvent 객체들의 리스트
     */
    public void saveEvents(List<PetEvent> events) {
        for (PetEvent event : events) {
            // 이벤트 해시값으로 DB에 존재 여부 조회
            boolean exists = repository.findByHash(event.getHash()).isPresent();
            if (!exists) {
                // DB에 해당 해시의 이벤트가 없으면 저장
                repository.save(event);
            }
            // 이미 존재하면 저장하지 않음 (중복 방지)
        }
    }

}
