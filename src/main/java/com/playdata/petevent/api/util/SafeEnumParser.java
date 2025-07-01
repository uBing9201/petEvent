package com.playdata.petevent.api.util;

import com.playdata.petevent.api.entity.AnimalsEntity;

public class SafeEnumParser {

    // 성별 코드가 유효하지 않으면 Q(미상)으로 대체
    public static AnimalsEntity.SexCode parseSexCode(String code) {
        try {
            return AnimalsEntity.SexCode.valueOf(code);
        } catch (Exception e) {
            return AnimalsEntity.SexCode.Q;
        }
    }

    // 중성화 여부가 유효하지 않으면 U(미상)으로 대체
    public static AnimalsEntity.NeuterYn parseNeuterYn(String code) {
        try {
            return AnimalsEntity.NeuterYn.valueOf(code);
        } catch (Exception e) {
            return AnimalsEntity.NeuterYn.U;
        }
    }
}