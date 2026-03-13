package com.iflytek.astron.console.hub.controller.point;

import com.iflytek.astron.console.commons.response.ApiResult;
import com.iflytek.astron.console.commons.util.RequestContextUtil;
import com.iflytek.astron.console.hub.dto.point.ResourceBalanceVO;
import com.iflytek.astron.console.hub.dto.point.ResourceDeductRequest;
import com.iflytek.astron.console.hub.dto.point.ResourceFlowVO;
import com.iflytek.astron.console.hub.dto.point.ResourceGrantRequest;
import com.iflytek.astron.console.hub.dto.point.ResourceInvalidateMemberRequest;
import com.iflytek.astron.console.hub.dto.point.ResourceManualDeductRequest;
import com.iflytek.astron.console.hub.dto.point.ResourceOverrideRequest;
import com.iflytek.astron.console.hub.dto.point.ResourceQueryRequest;
import com.iflytek.astron.console.hub.dto.point.ResourceRefundRequest;
import com.iflytek.astron.console.hub.service.point.ResourceService;
import com.iflytek.astron.console.hub.validation.point.ValidResourceType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/resource")
@Tag(name = "Resource Points")
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService resourceService;

    @PostMapping("/grant")
    @Operation(summary = "Grant resource points")
    public ApiResult<Void> grantResource(@Valid @RequestBody ResourceGrantRequest request) {
        request.setUid(RequestContextUtil.getUID());
        resourceService.grantResource(request);
        return ApiResult.success();
    }

    @PostMapping("/deduct")
    @Operation(summary = "Deduct resource points")
    public ApiResult<Void> deductResource(@Valid @RequestBody ResourceDeductRequest request) {
        request.setUid(RequestContextUtil.getUID());
        resourceService.deductResource(request);
        return ApiResult.success();
    }

    @PostMapping("/manual-deduct")
    @Operation(summary = "Manually deduct resource points")
    public ApiResult<Void> manualDeductResource(@Valid @RequestBody ResourceManualDeductRequest request) {
        request.setUid(RequestContextUtil.getUID());
        resourceService.manualDeductResource(request);
        return ApiResult.success();
    }

    @GetMapping("/balance")
    @Operation(summary = "Get current user's resource balance")
    public ApiResult<ResourceBalanceVO> getResourceBalance(@ValidResourceType @RequestParam String resourceType) {
        return ApiResult.success(resourceService.getResourceBalance(RequestContextUtil.getUID(), resourceType));
    }

    @PostMapping("/flow/query")
    @Operation(summary = "Query current user's resource flows")
    public ApiResult<List<ResourceFlowVO>> queryResourceFlow(@Valid @RequestBody ResourceQueryRequest request) {
        request.setUid(RequestContextUtil.getUID());
        return ApiResult.success(resourceService.queryResourceFlow(request));
    }

    @PostMapping("/invalidate-member")
    @Operation(summary = "Invalidate current user's source-specific resource points")
    public ApiResult<Void> invalidateMemberResource(@Valid @RequestBody ResourceInvalidateMemberRequest request) {
        request.setUid(RequestContextUtil.getUID());
        resourceService.invalidateMemberResource(request);
        return ApiResult.success();
    }

    @PostMapping("/override")
    @Operation(summary = "Override current user's resource points")
    public ApiResult<Void> overrideResource(@Valid @RequestBody ResourceOverrideRequest request) {
        request.setUid(RequestContextUtil.getUID());
        resourceService.overrideResource(request);
        return ApiResult.success();
    }

    @PostMapping("/refund")
    @Operation(summary = "Refund resource points by original deduct request")
    public ApiResult<Void> refundResource(@Valid @RequestBody ResourceRefundRequest request) {
        resourceService.refundResource(request);
        return ApiResult.success();
    }
}
