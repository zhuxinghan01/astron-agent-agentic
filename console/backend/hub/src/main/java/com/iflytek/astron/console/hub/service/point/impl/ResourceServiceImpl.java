package com.iflytek.astron.console.hub.service.point.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.iflytek.astron.console.commons.constant.ResponseEnum;
import com.iflytek.astron.console.commons.exception.BusinessException;
import com.iflytek.astron.console.hub.dto.point.ResourceBalanceVO;
import com.iflytek.astron.console.hub.dto.point.ResourceDeductRequest;
import com.iflytek.astron.console.hub.dto.point.ResourceFlowVO;
import com.iflytek.astron.console.hub.dto.point.ResourceGrantRequest;
import com.iflytek.astron.console.hub.dto.point.ResourceInvalidateMemberRequest;
import com.iflytek.astron.console.hub.dto.point.ResourceManualDeductRequest;
import com.iflytek.astron.console.hub.dto.point.ResourceOverrideRequest;
import com.iflytek.astron.console.hub.dto.point.ResourceQueryRequest;
import com.iflytek.astron.console.hub.dto.point.ResourceRefundRequest;
import com.iflytek.astron.console.hub.entity.point.ResourceDeductDetail;
import com.iflytek.astron.console.hub.entity.point.ResourceFlow;
import com.iflytek.astron.console.hub.entity.point.ResourceRecord;
import com.iflytek.astron.console.hub.entity.point.ResourceRequest;
import com.iflytek.astron.console.hub.enums.point.Direction;
import com.iflytek.astron.console.hub.enums.point.OperationType;
import com.iflytek.astron.console.hub.enums.point.RequestStatus;
import com.iflytek.astron.console.hub.enums.point.ResourceStatus;
import com.iflytek.astron.console.hub.enums.point.ResourceType;
import com.iflytek.astron.console.hub.enums.point.SourceType;
import com.iflytek.astron.console.hub.mapper.point.ResourceDeductDetailMapper;
import com.iflytek.astron.console.hub.mapper.point.ResourceFlowMapper;
import com.iflytek.astron.console.hub.mapper.point.ResourceRecordMapper;
import com.iflytek.astron.console.hub.mapper.point.ResourceRequestMapper;
import com.iflytek.astron.console.hub.service.point.ResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {
    private static final String RESOURCE_BALANCE_LOCK_PREFIX = "resource:balance:lock:";
    private static final long RESOURCE_LOCK_WAIT_SECONDS = 10L;
    private static final String DEFAULT_INVALIDATE_MEMBER_REASON = "Member resource invalidated";
    private static final String OVERRIDE_INVALIDATE_REASON = "Resource override - old resource invalidated";

    private final ResourceRecordMapper resourceRecordMapper;
    private final ResourceFlowMapper resourceFlowMapper;
    private final ResourceRequestMapper resourceRequestMapper;
    private final ResourceDeductDetailMapper resourceDeductDetailMapper;
    private final RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantResource(ResourceGrantRequest request) {
        if (checkAndHandleIdempotent(request.getResourceType(), request.getRequestId(), request.getRequestType())) {
            return;
        }

        ResourceRequest resourceRequest = createIdempotentRequest(
                request.getUid(),
                request.getResourceType(),
                request.getRequestId(),
                request.getRequestType(),
                request.getBizId(),
                OperationType.ADD.getCode());

        try {
            LocalDateTime now = LocalDateTime.now();
            ResourceRecord record = new ResourceRecord();
            record.setUid(request.getUid());
            record.setResourceType(request.getResourceType());
            record.setSourceType(request.getSourceType());
            record.setTotalAmount(request.getAmount());
            record.setUsedAmount(0L);
            record.setRemainAmount(request.getAmount());
            record.setGrantTime(now);
            record.setExpireTime(request.getExpireTime());
            record.setStatus(ResourceStatus.VALID.getCode());
            record.setVersion(0L);
            record.setCreateTime(now);
            record.setUpdateTime(now);
            resourceRecordMapper.insert(record);

            resourceFlowMapper.insert(buildFlow(
                    request.getUid(),
                    request.getResourceType(),
                    record.getId(),
                    Direction.ADD,
                    request.getAmount(),
                    request.getRequestId(),
                    request.getOperatorName(),
                    request.getReason(),
                    request.getRemark(),
                    1));

            updateRequestStatus(resourceRequest.getId(), RequestStatus.SUCCESS.getCode());
        } catch (Exception ex) {
            updateRequestStatus(resourceRequest.getId(), RequestStatus.FAILED.getCode());
            throw ex;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductResource(ResourceDeductRequest request) {
        String lockKey = buildResourceLockKey(request.getUid(), request.getResourceType());
        try (ResourceLock ignored = lockResource(lockKey, request.getUid(), request.getResourceType())) {
            if (checkAndHandleIdempotent(request.getResourceType(), request.getRequestId(), request.getRequestType())) {
                return;
            }

            ResourceRequest resourceRequest = createIdempotentRequest(
                    request.getUid(),
                    request.getResourceType(),
                    request.getRequestId(),
                    request.getRequestType(),
                    request.getBizId(),
                    OperationType.DEDUCT.getCode());

            try {
                List<ResourceDeductDetail> deductDetails = performDeduct(
                        request.getUid(),
                        request.getResourceType(),
                        request.getAmount());
                long actualDeductAmount = deductDetails.stream().mapToLong(ResourceDeductDetail::getAmount).sum();

                ResourceFlow flow = buildFlow(
                        request.getUid(),
                        request.getResourceType(),
                        null,
                        Direction.DEDUCT,
                        actualDeductAmount,
                        request.getRequestId(),
                        null,
                        request.getReason(),
                        request.getRemark(),
                        1);
                resourceFlowMapper.insert(flow);

                for (ResourceDeductDetail detail : deductDetails) {
                    detail.setFlowId(flow.getId());
                    resourceDeductDetailMapper.insert(detail);
                }

                updateRequestStatus(resourceRequest.getId(), RequestStatus.SUCCESS.getCode());
            } catch (Exception ex) {
                updateRequestStatus(resourceRequest.getId(), RequestStatus.FAILED.getCode());
                throw ex;
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void manualDeductResource(ResourceManualDeductRequest request) {
        String lockKey = buildResourceLockKey(request.getUid(), request.getResourceType());
        try (ResourceLock ignored = lockResource(lockKey, request.getUid(), request.getResourceType())) {
            if (checkAndHandleIdempotent(request.getResourceType(), request.getRequestId(), request.getRequestType())) {
                return;
            }

            ResourceRequest resourceRequest = createIdempotentRequest(
                    request.getUid(),
                    request.getResourceType(),
                    request.getRequestId(),
                    request.getRequestType(),
                    null,
                    OperationType.MANUAL_DEDUCT.getCode());

            try {
                List<ResourceDeductDetail> deductDetails = performDeduct(
                        request.getUid(),
                        request.getResourceType(),
                        request.getAmount());
                long actualDeductAmount = deductDetails.stream().mapToLong(ResourceDeductDetail::getAmount).sum();

                ResourceFlow flow = buildFlow(
                        request.getUid(),
                        request.getResourceType(),
                        null,
                        Direction.DEDUCT,
                        actualDeductAmount,
                        request.getRequestId(),
                        request.getOperatorName(),
                        request.getReason(),
                        request.getRemark(),
                        1);
                resourceFlowMapper.insert(flow);

                for (ResourceDeductDetail detail : deductDetails) {
                    detail.setFlowId(flow.getId());
                    resourceDeductDetailMapper.insert(detail);
                }

                updateRequestStatus(resourceRequest.getId(), RequestStatus.SUCCESS.getCode());
            } catch (Exception ex) {
                updateRequestStatus(resourceRequest.getId(), RequestStatus.FAILED.getCode());
                throw ex;
            }
        }
    }

    @Override
    public ResourceBalanceVO getResourceBalance(String uid, String resourceType) {
        ResourceBalanceVO balance = new ResourceBalanceVO();
        balance.setUid(uid);
        balance.setResourceType(resourceType);
        balance.setTotalAmount(0L);
        balance.setTotalBalance(0L);
        balance.setMemberTotal(0L);
        balance.setMemberBalance(0L);
        balance.setBuyTotal(0L);
        balance.setBuyBalance(0L);
        balance.setActivityTotal(0L);
        balance.setActivityBalance(0L);

        LambdaQueryWrapper<ResourceRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceRecord::getUid, uid)
                .eq(ResourceRecord::getResourceType, resourceType)
                .eq(ResourceRecord::getStatus, ResourceStatus.VALID.getCode())
                .and(w -> w.isNull(ResourceRecord::getExpireTime)
                        .or()
                        .gt(ResourceRecord::getExpireTime, LocalDateTime.now()));

        List<ResourceRecord> records = resourceRecordMapper.selectList(wrapper);
        for (ResourceRecord record : records) {
            long total = defaultLong(record.getTotalAmount());
            long remain = defaultLong(record.getRemainAmount());

            balance.setTotalAmount(balance.getTotalAmount() + total);
            balance.setTotalBalance(balance.getTotalBalance() + remain);

            if (SourceType.MEMBER.getCode().equals(record.getSourceType()) || SourceType.MANUAL.getCode().equals(record.getSourceType())) {
                balance.setMemberTotal(balance.getMemberTotal() + total);
                balance.setMemberBalance(balance.getMemberBalance() + remain);
            } else if (SourceType.BUY.getCode().equals(record.getSourceType())) {
                balance.setBuyTotal(balance.getBuyTotal() + total);
                balance.setBuyBalance(balance.getBuyBalance() + remain);
            } else if (SourceType.ACTIVITY.getCode().equals(record.getSourceType())) {
                balance.setActivityTotal(balance.getActivityTotal() + total);
                balance.setActivityBalance(balance.getActivityBalance() + remain);
            }
        }
        return balance;
    }

    @Override
    public List<ResourceFlowVO> queryResourceFlow(ResourceQueryRequest request) {
        LambdaQueryWrapper<ResourceFlow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceFlow::getUid, request.getUid())
                .eq(ResourceFlow::getResourceType, request.getResourceType())
                .ne(ResourceFlow::getVisible, 0);

        if (request.getDirection() != null) {
            wrapper.eq(ResourceFlow::getDirection, request.getDirection());
        }
        if (request.getStartTime() != null) {
            wrapper.ge(ResourceFlow::getCreateTime, request.getStartTime());
        }
        if (request.getEndTime() != null) {
            wrapper.le(ResourceFlow::getCreateTime, request.getEndTime());
        }

        int offset = Math.max(0, (request.getPageNum() - 1) * request.getPageSize());
        wrapper.orderByDesc(ResourceFlow::getCreateTime).last("LIMIT " + offset + ", " + request.getPageSize());

        List<ResourceFlow> flows = resourceFlowMapper.selectList(wrapper);
        List<ResourceFlowVO> results = new ArrayList<>(flows.size());
        for (ResourceFlow flow : flows) {
            ResourceFlowVO vo = new ResourceFlowVO();
            vo.setId(flow.getId());
            vo.setFlowNo(flow.getFlowNo());
            vo.setUid(flow.getUid());
            vo.setResourceType(flow.getResourceType());
            vo.setResourceTypeName(ResourceType.fromCode(flow.getResourceType()).getDesc());
            vo.setDirection(flow.getDirection());
            vo.setDirectionName(Direction.fromCode(flow.getDirection()).getDesc());
            vo.setAmount(flow.getAmount());
            vo.setReason(flow.getReason());
            vo.setRemark(flow.getRemark());
            vo.setCreateTime(flow.getCreateTime());

            if (Direction.ADD.getCode().equals(flow.getDirection()) && flow.getRecordId() != null) {
                ResourceRecord record = resourceRecordMapper.selectById(flow.getRecordId());
                if (record != null && StringUtils.hasText(record.getSourceType())) {
                    vo.setSourceType(record.getSourceType());
                    vo.setSourceTypeName(SourceType.fromCode(record.getSourceType()).getDesc());
                }
            }
            results.add(vo);
        }
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void invalidateMemberResource(ResourceInvalidateMemberRequest request) {
        String uid = request.getUid();
        String resourceType = request.getResourceType();
        String sourceType = StringUtils.hasText(request.getSourceType()) ? request.getSourceType() : SourceType.MEMBER.getCode();
        String lockKey = buildResourceLockKey(uid, resourceType);

        try (ResourceLock ignored = lockResource(lockKey, uid, resourceType)) {
            if (checkAndHandleIdempotent(resourceType, request.getRequestId(), request.getRequestType())) {
                return;
            }

            ResourceRequest resourceRequest = createIdempotentRequest(
                    uid,
                    resourceType,
                    request.getRequestId(),
                    request.getRequestType(),
                    request.getBizId(),
                    OperationType.INVALIDATE.getCode());

            try {
                LambdaQueryWrapper<ResourceRecord> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(ResourceRecord::getUid, uid)
                        .eq(ResourceRecord::getResourceType, resourceType)
                        .eq(ResourceRecord::getSourceType, sourceType)
                        .eq(ResourceRecord::getStatus, ResourceStatus.VALID.getCode());
                List<ResourceRecord> records = resourceRecordMapper.selectList(queryWrapper);

                LambdaUpdateWrapper<ResourceRecord> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(ResourceRecord::getUid, uid)
                        .eq(ResourceRecord::getResourceType, resourceType)
                        .eq(ResourceRecord::getSourceType, sourceType)
                        .eq(ResourceRecord::getStatus, ResourceStatus.VALID.getCode())
                        .set(ResourceRecord::getStatus, ResourceStatus.INVALID.getCode())
                        .set(ResourceRecord::getUpdateTime, LocalDateTime.now());
                resourceRecordMapper.update(null, updateWrapper);

                String reason = StringUtils.hasText(request.getReason()) ? request.getReason() : DEFAULT_INVALIDATE_MEMBER_REASON;
                for (ResourceRecord record : records) {
                    if (defaultLong(record.getRemainAmount()) <= 0) {
                        continue;
                    }
                    resourceFlowMapper.insert(buildFlow(
                            uid,
                            resourceType,
                            record.getId(),
                            Direction.FAILURE,
                            record.getRemainAmount(),
                            request.getRequestId(),
                            request.getOperatorName(),
                            reason,
                            request.getRemark(),
                            0));
                }

                updateRequestStatus(resourceRequest.getId(), RequestStatus.SUCCESS.getCode());
            } catch (Exception ex) {
                updateRequestStatus(resourceRequest.getId(), RequestStatus.FAILED.getCode());
                throw ex;
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void overrideResource(ResourceOverrideRequest request) {
        String lockKey = buildResourceLockKey(request.getUid(), request.getResourceType());
        try (ResourceLock ignored = lockResource(lockKey, request.getUid(), request.getResourceType())) {
            if (checkAndHandleIdempotent(request.getResourceType(), request.getRequestId(), request.getRequestType())) {
                return;
            }

            ResourceRequest resourceRequest = createIdempotentRequest(
                    request.getUid(),
                    request.getResourceType(),
                    request.getRequestId(),
                    request.getRequestType(),
                    request.getBizId(),
                    OperationType.OVERRIDE.getCode());

            try {
                LambdaQueryWrapper<ResourceRecord> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(ResourceRecord::getUid, request.getUid())
                        .eq(ResourceRecord::getResourceType, request.getResourceType())
                        .eq(ResourceRecord::getStatus, ResourceStatus.VALID.getCode());
                List<ResourceRecord> existingRecords = resourceRecordMapper.selectList(queryWrapper);

                if (!existingRecords.isEmpty()) {
                    LambdaUpdateWrapper<ResourceRecord> invalidWrapper = new LambdaUpdateWrapper<>();
                    invalidWrapper.eq(ResourceRecord::getUid, request.getUid())
                            .eq(ResourceRecord::getResourceType, request.getResourceType())
                            .eq(ResourceRecord::getStatus, ResourceStatus.VALID.getCode())
                            .set(ResourceRecord::getStatus, ResourceStatus.INVALID.getCode())
                            .set(ResourceRecord::getUpdateTime, LocalDateTime.now());
                    resourceRecordMapper.update(null, invalidWrapper);

                    for (ResourceRecord existing : existingRecords) {
                        if (defaultLong(existing.getRemainAmount()) <= 0) {
                            continue;
                        }
                        resourceFlowMapper.insert(buildFlow(
                                request.getUid(),
                                request.getResourceType(),
                                existing.getId(),
                                Direction.FAILURE,
                                existing.getRemainAmount(),
                                request.getRequestId(),
                                request.getOperatorName(),
                                OVERRIDE_INVALIDATE_REASON,
                                request.getRemark(),
                                0));
                    }
                }

                LocalDateTime now = LocalDateTime.now();
                ResourceRecord newRecord = new ResourceRecord();
                newRecord.setUid(request.getUid());
                newRecord.setResourceType(request.getResourceType());
                newRecord.setSourceType(request.getSourceType());
                newRecord.setTotalAmount(request.getAmount());
                newRecord.setUsedAmount(0L);
                newRecord.setRemainAmount(request.getAmount());
                newRecord.setGrantTime(now);
                newRecord.setExpireTime(request.getExpireTime());
                newRecord.setStatus(ResourceStatus.VALID.getCode());
                newRecord.setVersion(0L);
                newRecord.setCreateTime(now);
                newRecord.setUpdateTime(now);
                resourceRecordMapper.insert(newRecord);

                resourceFlowMapper.insert(buildFlow(
                        request.getUid(),
                        request.getResourceType(),
                        newRecord.getId(),
                        Direction.ADD,
                        request.getAmount(),
                        request.getRequestId(),
                        request.getOperatorName(),
                        request.getReason(),
                        request.getRemark(),
                        1));

                updateRequestStatus(resourceRequest.getId(), RequestStatus.SUCCESS.getCode());
            } catch (Exception ex) {
                updateRequestStatus(resourceRequest.getId(), RequestStatus.FAILED.getCode());
                throw ex;
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundResource(ResourceRefundRequest request) {
        LambdaQueryWrapper<ResourceFlow> flowWrapper = new LambdaQueryWrapper<>();
        flowWrapper.eq(ResourceFlow::getRequestId, request.getOriginalRequestId())
                .eq(ResourceFlow::getDirection, Direction.DEDUCT.getCode())
                .last("LIMIT 1");
        ResourceFlow originalFlow = resourceFlowMapper.selectOne(flowWrapper);
        if (originalFlow == null) {
            throw new BusinessException(ResponseEnum.RESOURCE_ORIGINAL_DEDUCT_NOT_FOUND);
        }

        String lockKey = buildResourceLockKey(originalFlow.getUid(), originalFlow.getResourceType());
        try (ResourceLock ignored = lockResource(lockKey, originalFlow.getUid(), originalFlow.getResourceType())) {
            if (checkAndHandleIdempotent(originalFlow.getResourceType(), request.getRequestId(), request.getRequestType())) {
                return;
            }

            ResourceRequest resourceRequest = createIdempotentRequest(
                    originalFlow.getUid(),
                    originalFlow.getResourceType(),
                    request.getRequestId(),
                    request.getRequestType(),
                    request.getBizId(),
                    OperationType.REFUND.getCode());

            try {
                LambdaQueryWrapper<ResourceDeductDetail> detailWrapper = new LambdaQueryWrapper<>();
                detailWrapper.eq(ResourceDeductDetail::getFlowId, originalFlow.getId());
                List<ResourceDeductDetail> deductDetails = resourceDeductDetailMapper.selectList(detailWrapper);
                if (deductDetails.isEmpty()) {
                    throw new BusinessException(ResponseEnum.RESOURCE_DEDUCT_DETAIL_NOT_FOUND);
                }

                long totalRefundAmount = 0L;
                for (ResourceDeductDetail detail : deductDetails) {
                    ResourceRecord record = resourceRecordMapper.selectById(detail.getRecordId());
                    if (record == null) {
                        throw new BusinessException(ResponseEnum.RESOURCE_RECORD_NOT_FOUND);
                    }

                    LambdaUpdateWrapper<ResourceRecord> updateWrapper = new LambdaUpdateWrapper<>();
                    updateWrapper.eq(ResourceRecord::getId, record.getId())
                            .eq(ResourceRecord::getVersion, record.getVersion())
                            .set(ResourceRecord::getUsedAmount, defaultLong(record.getUsedAmount()) - detail.getAmount())
                            .set(ResourceRecord::getRemainAmount, defaultLong(record.getRemainAmount()) + detail.getAmount())
                            .set(ResourceRecord::getVersion, defaultLong(record.getVersion()) + 1)
                            .set(ResourceRecord::getUpdateTime, LocalDateTime.now());

                    if (ResourceStatus.USED_UP.getCode().equals(record.getStatus())
                            || ResourceStatus.INVALID.getCode().equals(record.getStatus())) {
                        updateWrapper.set(ResourceRecord::getStatus, ResourceStatus.VALID.getCode());
                    }

                    int updated = resourceRecordMapper.update(null, updateWrapper);
                    if (updated == 0) {
                        throw new BusinessException(ResponseEnum.RESOURCE_REFUND_CONCURRENT_CONFLICT);
                    }
                    totalRefundAmount += detail.getAmount();
                }

                resourceFlowMapper.insert(buildFlow(
                        originalFlow.getUid(),
                        originalFlow.getResourceType(),
                        null,
                        Direction.REFUND,
                        totalRefundAmount,
                        request.getRequestId(),
                        null,
                        request.getReason(),
                        request.getRemark(),
                        1));

                updateRequestStatus(resourceRequest.getId(), RequestStatus.SUCCESS.getCode());
            } catch (Exception ex) {
                updateRequestStatus(resourceRequest.getId(), RequestStatus.FAILED.getCode());
                throw ex;
            }
        }
    }

    private List<ResourceDeductDetail> performDeduct(String uid, String resourceType, Long amount) {
        LambdaQueryWrapper<ResourceRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceRecord::getUid, uid)
                .eq(ResourceRecord::getResourceType, resourceType)
                .eq(ResourceRecord::getStatus, ResourceStatus.VALID.getCode())
                .gt(ResourceRecord::getRemainAmount, 0)
                .and(w -> w.isNull(ResourceRecord::getExpireTime)
                        .or()
                        .gt(ResourceRecord::getExpireTime, LocalDateTime.now()));

        List<ResourceRecord> records = resourceRecordMapper.selectList(wrapper);
        if (records.isEmpty()) {
            throw new BusinessException(ResponseEnum.RESOURCE_INSUFFICIENT);
        }

        records.sort(Comparator
                .comparingInt((ResourceRecord record) -> getSourceTypePriority(record.getSourceType()))
                .reversed()
                .thenComparing(ResourceRecord::getExpireTime, Comparator.nullsLast(Comparator.naturalOrder())));

        long totalAvailable = records.stream().mapToLong(ResourceRecord::getRemainAmount).sum();
        long targetDeductAmount = Math.min(amount, totalAvailable);
        if (targetDeductAmount <= 0) {
            throw new BusinessException(ResponseEnum.RESOURCE_INSUFFICIENT);
        }

        List<ResourceDeductDetail> details = new ArrayList<>();
        long remainToDeduct = targetDeductAmount;
        for (ResourceRecord record : records) {
            if (remainToDeduct <= 0) {
                break;
            }

            long canDeduct = Math.min(remainToDeduct, record.getRemainAmount());
            LambdaUpdateWrapper<ResourceRecord> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ResourceRecord::getId, record.getId())
                    .eq(ResourceRecord::getVersion, record.getVersion())
                    .set(ResourceRecord::getUsedAmount, defaultLong(record.getUsedAmount()) + canDeduct)
                    .set(ResourceRecord::getRemainAmount, defaultLong(record.getRemainAmount()) - canDeduct)
                    .set(ResourceRecord::getVersion, defaultLong(record.getVersion()) + 1)
                    .set(ResourceRecord::getUpdateTime, LocalDateTime.now());
            if (record.getRemainAmount() - canDeduct == 0) {
                updateWrapper.set(ResourceRecord::getStatus, ResourceStatus.USED_UP.getCode());
            }

            int updated = resourceRecordMapper.update(null, updateWrapper);
            if (updated == 0) {
                throw new BusinessException(ResponseEnum.RESOURCE_CONCURRENT_CONFLICT);
            }

            ResourceDeductDetail detail = new ResourceDeductDetail();
            detail.setRecordId(record.getId());
            detail.setUid(uid);
            detail.setResourceType(resourceType);
            detail.setAmount(canDeduct);
            detail.setCreateTime(LocalDateTime.now());
            details.add(detail);

            remainToDeduct -= canDeduct;
        }
        return details;
    }

    private int getSourceTypePriority(String sourceType) {
        if (SourceType.ACTIVITY.getCode().equals(sourceType)) {
            return 3;
        }
        if (SourceType.MEMBER.getCode().equals(sourceType) || SourceType.MANUAL.getCode().equals(sourceType)) {
            return 2;
        }
        if (SourceType.BUY.getCode().equals(sourceType)) {
            return 1;
        }
        return 0;
    }

    private ResourceRequest checkIdempotent(String resourceType, String requestId, String requestType) {
        LambdaQueryWrapper<ResourceRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceRequest::getResourceType, resourceType)
                .eq(ResourceRequest::getRequestType, requestType)
                .eq(ResourceRequest::getRequestId, requestId)
                .last("LIMIT 1");
        return resourceRequestMapper.selectOne(wrapper);
    }

    private boolean checkAndHandleIdempotent(String resourceType, String requestId, String requestType) {
        ResourceRequest existingRequest = checkIdempotent(resourceType, requestId, requestType);
        if (existingRequest == null) {
            return false;
        }
        if (RequestStatus.SUCCESS.getCode().equals(existingRequest.getStatus())) {
            return true;
        }
        if (RequestStatus.PROCESSING.getCode().equals(existingRequest.getStatus())) {
            throw new BusinessException(ResponseEnum.RESOURCE_REQUEST_IN_PROCESS);
        }
        return false;
    }

    private ResourceRequest createIdempotentRequest(
            String uid,
            String resourceType,
            String requestId,
            String requestType,
            String bizId,
            String operationType) {
        ResourceRequest resourceRequest = new ResourceRequest();
        resourceRequest.setUid(uid);
        resourceRequest.setResourceType(resourceType);
        resourceRequest.setRequestId(requestId);
        resourceRequest.setRequestType(requestType);
        resourceRequest.setBizId(bizId);
        resourceRequest.setOperationType(operationType);
        resourceRequest.setStatus(RequestStatus.PROCESSING.getCode());
        resourceRequest.setCreateTime(LocalDateTime.now());
        resourceRequest.setUpdateTime(LocalDateTime.now());
        try {
            resourceRequestMapper.insert(resourceRequest);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(ResponseEnum.RESOURCE_IDEMPOTENT_DUPLICATE);
        }
        return resourceRequest;
    }

    private void updateRequestStatus(Long requestId, Integer status) {
        LambdaUpdateWrapper<ResourceRequest> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ResourceRequest::getId, requestId)
                .set(ResourceRequest::getStatus, status)
                .set(ResourceRequest::getUpdateTime, LocalDateTime.now());
        resourceRequestMapper.update(null, updateWrapper);
    }

    private ResourceFlow buildFlow(
            String uid,
            String resourceType,
            Long recordId,
            Direction direction,
            Long amount,
            String requestId,
            String operatorName,
            String reason,
            String remark,
            Integer visible) {
        ResourceFlow flow = new ResourceFlow();
        flow.setFlowNo(generateFlowNo());
        flow.setUid(uid);
        flow.setResourceType(resourceType);
        flow.setRecordId(recordId);
        flow.setDirection(direction.getCode());
        flow.setAmount(amount);
        flow.setRequestId(requestId);
        flow.setOperatorName(operatorName);
        flow.setReason(reason);
        flow.setRemark(remark);
        flow.setVisible(visible);
        flow.setCreateTime(LocalDateTime.now());
        flow.setUpdateTime(LocalDateTime.now());
        return flow;
    }

    private String generateFlowNo() {
        return "FLOW_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private String buildResourceLockKey(String uid, String resourceType) {
        return RESOURCE_BALANCE_LOCK_PREFIX + uid + ":" + resourceType;
    }

    private ResourceLock lockResource(String lockKey, String uid, String resourceType) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean locked = lock.tryLock(RESOURCE_LOCK_WAIT_SECONDS, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("Failed to acquire resource lock. uid={}, resourceType={}, lockKey={}", uid, resourceType, lockKey);
                throw new BusinessException(ResponseEnum.RESOURCE_REQUEST_IN_PROCESS);
            }
            return new ResourceLock(lock);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ResponseEnum.RESOURCE_REQUEST_IN_PROCESS, ex);
        }
    }

    private long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private static final class ResourceLock implements AutoCloseable {
        private final RLock lock;

        private ResourceLock(RLock lock) {
            this.lock = lock;
        }

        @Override
        public void close() {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
