package com.playdata.petevent.util.repository;

import com.playdata.petevent.util.entity.PetEvent;
import com.playdata.petevent.util.repository.custom.PetRepositoryCustom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PetEvent 엔티티에 대한 JPA 리포지토리 인터페이스
 * JpaRepository를 상속받아 기본 CRUD 기능 제공
 * 커스텀 리포지토리 인터페이스 PetRepositoryCustom도 함께 상속
 */
public interface PetRepository extends JpaRepository<PetEvent, Long>, PetRepositoryCustom {

    /**
     * 주어진 해시 값을 가진 PetEvent를 조회한다.
     * 중복 저장 방지를 위해 해시로 이벤트 존재 여부를 확인할 때 사용됨.
     *
     * @param hash 이벤트의 고유 해시 값
     * @return Optional로 감싼 PetEvent, 존재하지 않으면 Optional.empty()
     */
    Optional<PetEvent> findByHash(String hash);

}
