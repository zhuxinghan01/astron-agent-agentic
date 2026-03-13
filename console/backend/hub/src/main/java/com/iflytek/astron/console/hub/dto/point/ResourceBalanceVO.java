package com.iflytek.astron.console.hub.dto.point;

import lombok.Data;

@Data
public class ResourceBalanceVO {
    private String uid;

    private String resourceType;

    private Long totalAmount;

    private Long totalBalance;

    private Long memberTotal;

    private Long memberBalance;

    private Long buyTotal;

    private Long buyBalance;

    private Long activityTotal;

    private Long activityBalance;
}
