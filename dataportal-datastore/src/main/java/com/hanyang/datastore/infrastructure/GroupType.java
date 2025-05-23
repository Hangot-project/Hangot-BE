package com.hanyang.datastore.infrastructure;

import lombok.Getter;

@Getter
public enum GroupType {
    SUM(0),
    AVG(1);

    private final int code;

    GroupType(int code) {
        this.code = code;
    }

    public static GroupType fromCode(int code) {
        for (GroupType type : values()) {
            if (type.code == code) return type;
        }
        throw new IllegalArgumentException("Invalid GroupType code: " + code);
    }

}
