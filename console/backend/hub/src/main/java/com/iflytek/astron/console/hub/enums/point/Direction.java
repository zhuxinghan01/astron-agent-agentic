package com.iflytek.astron.console.hub.enums.point;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Direction {
    ADD(1, "Add"),
    DEDUCT(2, "Deduct"),
    FAILURE(3, "Invalidate"),
    REFUND(4, "Refund");

    private final Integer code;
    private final String desc;

    public static Direction fromCode(Integer code) {
        for (Direction direction : values()) {
            if (direction.code.equals(code)) {
                return direction;
            }
        }
        throw new IllegalArgumentException("Unknown direction: " + code);
    }
}
