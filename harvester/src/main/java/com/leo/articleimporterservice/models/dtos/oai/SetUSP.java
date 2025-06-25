package com.leo.articleimporterservice.models.dtos.oai;

import lombok.Getter;

@Getter
public enum SetUSP {

    PRODUCAOINTELECTUAL("PI",1),
    TESESEDISSERTACOES("TD",2),
    OPENACCESS("OA",3),
    ;
    private final String setSpec;
    private final int index;
    SetUSP(String setName, Integer id) {
        this.setSpec = setName;
        this.index = id;
    }

    public static String getSetSpecByIndex(int index) {
        for (SetUSP set : SetUSP.values()) {
            if (set.getIndex() == index) {
                return set.getSetSpec();
            }
        }
        return null;
    }

}
