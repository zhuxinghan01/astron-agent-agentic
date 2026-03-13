package com.iflytek.astron.console.hub.dto.point;

import com.iflytek.astron.console.hub.validation.point.ValidRequestType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResourceRefundRequest {
    @NotBlank(message = "{resource.request.id.required}")
    private String requestId;

    @NotBlank(message = "{resource.request.type.required}")
    @ValidRequestType
    private String requestType;

    @NotBlank(message = "{resource.original.request.id.required}")
    private String originalRequestId;

    private String bizId;

    private String reason;

    private String remark;
}
