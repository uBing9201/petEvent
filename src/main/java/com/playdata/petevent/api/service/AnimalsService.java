package com.playdata.petevent.api.service;

import com.playdata.petevent.api.entity.AnimalsEntity;
import java.util.List;

/**
 * AnimalsEntity 관련 비즈니스 로직 서비스 인터페이스
 */
public interface AnimalsService {

    /**
     * 저장 또는 업데이트 처리
     * @param entity 저장할 동물 엔티티
     * @return 저장 또는 업데이트된 동물 엔티티
     */
    AnimalsEntity saveOrUpdate(AnimalsEntity entity);

    /**
     * 보호중이 아닌 유기번호 리스트에 해당하는 데이터 삭제 처리
     * @param desertionNoList 삭제 대상 유기번호 목록
     */
    void removeIfNotProtect(List<String> desertionNoList);

}