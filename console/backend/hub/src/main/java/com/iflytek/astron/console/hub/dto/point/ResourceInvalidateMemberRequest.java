package com.iflytek.astron.console.hub.dto.point;

import com.iflytek.astron.console.hub.validation.point.ValidRequestType;
import com.iflytek.astron.console.hub.validation.point.ValidResourceType;
import com.iflytek.astron.console.hub.validation.point.ValidSourceType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResourceInvalidateMemberRequest {
    private String uid;

    @NotBlank(message = "{resource.type.required}")
    @ValidResourceType
    private String resourceType;

    @ValidSourceType
    private String sourceType;

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
