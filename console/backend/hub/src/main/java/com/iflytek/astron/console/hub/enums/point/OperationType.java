package com.iflytek.astron.console.hub.enums.point;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OperationType {
    ADD("ADD", "Grant"),
    DEDUCT("DEDUCT", "Deduct"),
    MANUAL_DEDUCT("MANUAL_DEDUCT", "Manual deduct"),
    OVERRIDE("OVERRIDE", "Override"),
    INVALIDATE("INVALIDATE", "Invalidate"),
    REFUND("REFUND", "Refund");

    private final String code;
    private final String desc;
}
