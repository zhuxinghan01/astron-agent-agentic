package com.iflytek.astron.console.hub.entity.point;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resource_flow")
public class ResourceFlow {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String flowNo;

    private String uid;

    private String resourceType;

    private Long recordId;

    private Integer direction;

    private Long amount;

    private String requestId;

    private String operatorName;

    private String reason;

    private String remark;

    private Integer visible;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
