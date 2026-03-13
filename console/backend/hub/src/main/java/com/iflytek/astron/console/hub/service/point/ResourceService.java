package com.iflytek.astron.console.hub.service.point;

import com.iflytek.astron.console.hub.dto.point.ResourceBalanceVO;
import com.iflytek.astron.console.hub.dto.point.ResourceDeductRequest;
import com.iflytek.astron.console.hub.dto.point.ResourceFlowVO;
import com.iflytek.astron.console.hub.dto.point.ResourceGrantRequest;
import com.iflytek.astron.console.hub.dto.point.ResourceInvalidateMemberRequest;
import com.iflytek.astron.console.hub.dto.point.ResourceManualDeductRequest;
import com.iflytek.astron.console.hub.dto.point.ResourceOverrideRequest;
import com.iflytek.astron.console.hub.dto.point.ResourceQueryRequest;
import com.iflytek.astron.console.hub.dto.point.ResourceRefundRequest;

import java.util.List;

public interface ResourceService {
    void grantResource(ResourceGrantRequest request);

    void deductResource(ResourceDeductRequest request);

    void manualDeductResource(ResourceManualDeductRequest request);

    ResourceBalanceVO getResourceBalance(String uid, String resourceType);

    List<ResourceFlowVO> queryResourceFlow(ResourceQueryRequest request);

    void invalidateMemberResource(ResourceInvalidateMemberRequest request);

    void overrideResource(ResourceOverrideRequest request);

    void refundResource(ResourceRefundRequest request);
}
