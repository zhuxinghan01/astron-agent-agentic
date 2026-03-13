package com.iflytek.astron.console.hub.dto.point;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResourceFlowVO {
    private Long id;

    private String flowNo;

    private String uid;

    private String resourceType;

    private String resourceTypeName;

    private Integer direction;

    private String directionName;

    private Long amount;

    private String sourceType;

    private String sourceTypeName;

    private String reason;

    private String remark;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
