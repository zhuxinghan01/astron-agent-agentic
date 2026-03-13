package com.iflytek.astron.console.hub.enums.point;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResourceStatus {
    VALID(1, "Valid"),
    USED_UP(2, "Used up"),
    INVALID(4, "Invalid");

    private final Integer code;
    private final String desc;
}
