package com.iflytek.astron.console.hub.entity.point;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resource_request")
public class ResourceRequest {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String uid;

    private String resourceType;

    private String requestId;

    private String requestType;

    private String bizId;

    private String operationType;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
