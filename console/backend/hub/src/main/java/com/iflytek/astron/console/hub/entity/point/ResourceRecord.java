package com.iflytek.astron.console.hub.entity.point;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("resource_record")
public class ResourceRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String uid;

    private String resourceType;

    private String sourceType;

    private Long totalAmount;

    private Long usedAmount;

    private Long remainAmount;

    private LocalDateTime grantTime;

    private LocalDateTime expireTime;

    private Integer status;

    @Version
    private Long version;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
