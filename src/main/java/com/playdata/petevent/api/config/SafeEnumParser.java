package com.playdata.petevent.api.config;

import com.playdata.petevent.api.entity.AnimalsEntity;

public class SafeEnumParser {

    public static AnimalsEntity.SexCode parseSexCode(String code) {
        try {
            return AnimalsEntity.SexCode.valueOf(code);
        } catch (Exception e) {
            return AnimalsEntity.SexCode.Q;
        }
    }

    public static AnimalsEntity.NeuterYn parseNeuterYn(String code) {
        try {
            return AnimalsEntity.NeuterYn.valueOf(code);
        } catch (Exception e) {
            return AnimalsEntity.NeuterYn.U;
        }
    }
}