package com.iflytek.astron.console.hub.enums.point;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RequestType {
    AGENT("AGENT", "Agent");

    private final String code;
    private final String desc;

    public static RequestType fromCode(String code) {
        for (RequestType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown request type: " + code);
    }
}
