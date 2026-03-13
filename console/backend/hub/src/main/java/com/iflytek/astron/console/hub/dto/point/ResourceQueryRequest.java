package com.iflytek.astron.console.hub.dto.point;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.iflytek.astron.console.hub.validation.point.ValidResourceType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResourceQueryRequest {
    private String uid;

    @NotBlank(message = "{resource.type.required}")
    @ValidResourceType
    private String resourceType;

    private Integer direction;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Min(value = 1, message = "{resource.query.page.invalid}")
    private Integer pageNum = 1;

    @Min(value = 1, message = "{resource.query.page.invalid}")
    @Max(value = 100, message = "{resource.query.page.invalid}")
    private Integer pageSize = 20;
}
