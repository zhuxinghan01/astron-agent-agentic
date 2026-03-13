package com.iflytek.astron.console.hub.dto.point;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iflytek.astron.console.hub.validation.point.ValidRequestType;
import com.iflytek.astron.console.hub.validation.point.ValidResourceType;
import com.iflytek.astron.console.hub.validation.point.ValidSourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResourceOverrideRequest {
    private String uid;

    @NotBlank(message = "{resource.type.required}")
    @ValidResourceType
    private String resourceType;

    @NotBlank(message = "{resource.source.type.required}")
    @ValidSourceType
    private String sourceType;

    @NotNull(message = "{resource.amount.required}")
    @Min(value = 1, message = "{resource.amount.min}")
    private Long amount;

    @NotNull(message = "{resource.expire.time.required}")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    @NotBlank(message = "{resource.request.id.required}")
    private String requestId;

    @NotBlank(message = "{resource.request.type.required}")
    @ValidRequestType
    private String requestType;

    private String bizId;

    private String operatorName;

    private String reason;

    private String remark;
}
