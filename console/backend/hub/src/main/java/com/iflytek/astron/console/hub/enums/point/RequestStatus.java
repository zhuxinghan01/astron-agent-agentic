package com.iflytek.astron.console.hub.enums.point;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RequestStatus {
    PROCESSING(0, "Processing"),
    SUCCESS(1, "Success"),
    FAILED(2, "Failed");

    private final Integer code;
    private final String desc;
}
