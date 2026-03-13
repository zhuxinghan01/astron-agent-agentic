package com.iflytek.astron.console.hub.entity.point;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resource_deduct_detail")
public class ResourceDeductDetail {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long flowId;

    private Long recordId;

    private String uid;

    private String resourceType;

    private Long amount;

    private LocalDateTime createTime;
}
