package com.iflytek.astron.console.hub.enums.point;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SourceType {
    MEMBER("MEMBER", "Member"),
    BUY("BUY", "Buy"),
    ACTIVITY("ACTIVITY", "Activity"),
    MANUAL("MANUAL", "Manual");

    private final String code;
    private final String desc;

    public static SourceType fromCode(String code) {
        for (SourceType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown source type: " + code);
    }
}
