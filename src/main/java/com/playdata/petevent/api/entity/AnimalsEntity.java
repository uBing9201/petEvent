package com.playdata.petevent.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 유기동물 정보 DB 엔티티.
 * JPA Entity 어노테이션 기반 매핑.
 *
 * 주요 필드:
 * - desertionNo: 유기번호, 기본키
 * - processState: 보호 상태 (보호중, 분양중 등)
 * - sexCd, neuterYn: enum으로 관리 (성별, 중성화 여부)
 * - createAt, updateAt: 생성/수정일 자동 관리
 *
 * updateIfChanged 메서드로 안전하게 필드 업데이트 가능.
 */
@Getter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "abandoned_animals")
@EntityListeners(AuditingEntityListener.class)
public class AnimalsEntity {

    @Id
    @Column(name = "desertion_no", nullable = false, unique = true)
    private String desertionNo; // 유기동물 고유번호, PK 역할

    @Column(name = "rfid_cd")
    private String rfidCd; // 내장 칩 RFID 코드 (없을 수 있음)

    @Column(name = "happen_dt")
    private String happenDt; // 유기 발생 날짜 (YYYYMMDD 형식)

    @Column(name = "happen_place")
    private String happenPlace; // 유기 발생 장소

    @Column(name = "up_kind_nm")
    private String upKindNm; // 축종 이름 (예: 개, 고양이)

    @Column(name = "kind_nm")
    private String kindNm; // 품종 이름

    @Column(name = "color_cd")
    private String colorCd; // 털색 정보

    @Column(name = "age")
    private String age; // 나이 정보

    @Column(name = "weight")
    private String weight; // 체중 (문자열로 저장, 예: "3Kg")

    @Column(name = "notice_sdt")
    private String noticeSdt; // 공고 시작일

    @Column(name = "notice_edt")
    private String noticeEdt; // 공고 종료일

    @Column(name = "popfile1")
    private String popfile1; // 대표 이미지 URL

    @Column(name = "popfile2")
    private String popfile2; // 보조 이미지 URL

    @Column(name = "process_state")
    private String processState; // 보호 상태 (예: 보호중, 분양중)

    @Enumerated(EnumType.STRING)
    @Column(name = "sex_cd", length = 1)
    private SexCode sexCd; // 성별 코드 (M: 수컷, F: 암컷, Q: 미상)

    @Enumerated(EnumType.STRING)
    @Column(name = "neuter_yn", length = 1)
    private NeuterYn neuterYn; // 중성화 여부 (Y: 예, N: 아니오, U: 미상)

    @Column(name = "special_mark", columnDefinition = "TEXT")
    private String specialMark; // 특이사항

    @Column(name = "care_nm")
    private String careNm; // 보호소 이름

    @Column(name = "care_tel")
    private String careTel; // 보호소 전화번호

    @Column(name = "care_addr")
    private String careAddr; // 보호소 주소

    @Column(name = "care_owner_nm")
    private String careOwnerNm; // 보호 책임자명

    @Column(name = "org_nm")
    private String orgNm; // 보호소 관할 지자체 이름

    @Column(name = "etc_bigo", columnDefinition = "TEXT")
    private String etcBigo; // 기타 비고

    @CreatedDate
    @Column(name = "create_at", updatable = false)
    private LocalDateTime createAt; // 생성 일시 (자동 관리)

    @LastModifiedDate
    @Column(name = "update_at")
    private LocalDateTime updateAt; // 수정 일시 (자동 관리)

    /**
     * 기존 엔티티 필드와 비교해 source 값이 다르고 null이 아닐 때만 업데이트
     * desertionNo, id 등 식별키 필드는 제외하여 안전한 필드 변경 보장
     */
    public void updateIfChanged(AnimalsEntity source) {
        if (source.getRfidCd() != null && !source.getRfidCd().equals(this.rfidCd)) {
            this.rfidCd = source.getRfidCd();
        }
        if (source.getHappenDt() != null && !source.getHappenDt().equals(this.happenDt)) {
            this.happenDt = source.getHappenDt();
        }
        if (source.getHappenPlace() != null && !source.getHappenPlace().equals(this.happenPlace)) {
            this.happenPlace = source.getHappenPlace();
        }
        if (source.getUpKindNm() != null && !source.getUpKindNm().equals(this.upKindNm)) {
            this.upKindNm = source.getUpKindNm();
        }
        if (source.getKindNm() != null && !source.getKindNm().equals(this.kindNm)) {
            this.kindNm = source.getKindNm();
        }
        if (source.getColorCd() != null && !source.getColorCd().equals(this.colorCd)) {
            this.colorCd = source.getColorCd();
        }
        if (source.getAge() != null && !source.getAge().equals(this.age)) {
            this.age = source.getAge();
        }
        if (source.getWeight() != null && !source.getWeight().equals(this.weight)) {
            this.weight = source.getWeight();
        }
        if (source.getNoticeSdt() != null && !source.getNoticeSdt().equals(this.noticeSdt)) {
            this.noticeSdt = source.getNoticeSdt();
        }
        if (source.getNoticeEdt() != null && !source.getNoticeEdt().equals(this.noticeEdt)) {
            this.noticeEdt = source.getNoticeEdt();
        }
        if (source.getPopfile1() != null && !source.getPopfile1().equals(this.popfile1)) {
            this.popfile1 = source.getPopfile1();
        }
        if (source.getPopfile2() != null && !source.getPopfile2().equals(this.popfile2)) {
            this.popfile2 = source.getPopfile2();
        }
        if (source.getProcessState() != null && !source.getProcessState().equals(this.processState)) {
            this.processState = source.getProcessState();
        }
        if (source.getSexCd() != null && !source.getSexCd().equals(this.sexCd)) {
            this.sexCd = source.getSexCd();
        }
        if (source.getNeuterYn() != null && !source.getNeuterYn().equals(this.neuterYn)) {
            this.neuterYn = source.getNeuterYn();
        }
        if (source.getSpecialMark() != null && !source.getSpecialMark().equals(this.specialMark)) {
            this.specialMark = source.getSpecialMark();
        }
        if (source.getCareNm() != null && !source.getCareNm().equals(this.careNm)) {
            this.careNm = source.getCareNm();
        }
        if (source.getCareTel() != null && !source.getCareTel().equals(this.careTel)) {
            this.careTel = source.getCareTel();
        }
        if (source.getCareAddr() != null && !source.getCareAddr().equals(this.careAddr)) {
            this.careAddr = source.getCareAddr();
        }
        if (source.getCareOwnerNm() != null && !source.getCareOwnerNm().equals(this.careOwnerNm)) {
            this.careOwnerNm = source.getCareOwnerNm();
        }
        if (source.getOrgNm() != null && !source.getOrgNm().equals(this.orgNm)) {
            this.orgNm = source.getOrgNm();
        }
        if (source.getEtcBigo() != null && !source.getEtcBigo().equals(this.etcBigo)) {
            this.etcBigo = source.getEtcBigo();
        }
    }

    // 성별 코드 enum (M: 수컷, F: 암컷, Q: 미상)
    public enum SexCode {
        M,
        F,
        Q
    }

    // 중성화 여부 enum (Y: 예, N: 아니오, U: 미상)
    public enum NeuterYn {
        Y,
        N,
        U
    }
}