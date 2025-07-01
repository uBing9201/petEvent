package com.playdata.petevent.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 개별 유기동물 API 응답 항목 DTO
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@Builder
public class ApiAnimalItem {

    private String desertionNo;
    private String rfidCd;
    private String happenDt;
    private String happenPlace;
    private String upKindNm;
    private String kindNm;
    private String colorCd;
    private String age;
    private String weight;
    private String noticeSdt;
    private String noticeEdt;
    private String popfile;
    private String popfile2;
    private String processState;
    private String sexCd;
    private String neuterYn;
    private String specialMark;
    private String careNm;
    private String careTel;
    private String careAddr;
    private String careOwnerNm;
    private String orgNm;
    private String etcBigo;
}