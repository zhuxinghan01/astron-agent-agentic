package com.iflytek.astron.console.hub.dto.point;

import com.iflytek.astron.console.hub.validation.point.ValidRequestType;
import com.iflytek.astron.console.hub.validation.point.ValidResourceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResourceManualDeductRequest {
    private String uid;

    @NotBlank(message = "{resource.request.id.required}")
    private String requestId;

    @NotBlank(message = "{resource.type.required}")
    @ValidResourceType
    private String resourceType;

    @NotBlank(message = "{resource.request.type.required}")
    @ValidRequestType
    private String requestType;

    @NotNull(message = "{resource.amount.required}")
    @Min(value = 1, message = "{resource.amount.min}")
    private Long amount;

    private String operatorName;

    private String reason;

    private String remark;
}
