package com.playdata.petevent.api.repository;

import com.playdata.petevent.api.entity.AnimalsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * AnimalsEntity에 대한 DB 접근 인터페이스(JPA Repository)
 * 기본 CRUD 기능 제공 + desertionNo 기반 조회/삭제 기능 추가
 */
public interface AnimalsRepository extends JpaRepository<AnimalsEntity, String> {

    /**
     * desertionNo를 기준으로 동물 데이터 조회
     * @param desertionNo 유기번호 (유니크)
     * @return Optional<AnimalsEntity> - 존재하면 동물 엔티티 반환
     */
    Optional<AnimalsEntity> findByDesertionNo(String desertionNo);
}