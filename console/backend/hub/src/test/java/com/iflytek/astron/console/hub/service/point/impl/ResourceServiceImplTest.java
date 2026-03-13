package com.iflytek.astron.console.hub.service.point.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.iflytek.astron.console.commons.constant.ResponseEnum;
import com.iflytek.astron.console.commons.exception.BusinessException;
import com.iflytek.astron.console.hub.dto.point.ResourceDeductRequest;
import com.iflytek.astron.console.hub.dto.point.ResourceInvalidateMemberRequest;
import com.iflytek.astron.console.hub.dto.point.ResourceRefundRequest;
import com.iflytek.astron.console.hub.entity.point.ResourceDeductDetail;
import com.iflytek.astron.console.hub.entity.point.ResourceFlow;
import com.iflytek.astron.console.hub.entity.point.ResourceRecord;
import com.iflytek.astron.console.hub.entity.point.ResourceRequest;
import com.iflytek.astron.console.hub.enums.point.Direction;
import com.iflytek.astron.console.hub.enums.point.RequestStatus;
import com.iflytek.astron.console.hub.enums.point.ResourceStatus;
import com.iflytek.astron.console.hub.enums.point.SourceType;
import com.iflytek.astron.console.hub.mapper.point.ResourceDeductDetailMapper;
import com.iflytek.astron.console.hub.mapper.point.ResourceFlowMapper;
import com.iflytek.astron.console.hub.mapper.point.ResourceRecordMapper;
import com.iflytek.astron.console.hub.mapper.point.ResourceRequestMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceServiceImplTest {

    @Mock
    private ResourceRecordMapper resourceRecordMapper;

    @Mock
    private ResourceFlowMapper resourceFlowMapper;

    @Mock
    private ResourceRequestMapper resourceRequestMapper;

    @Mock
    private ResourceDeductDetailMapper resourceDeductDetailMapper;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock lock;

    @InjectMocks
    private ResourceServiceImpl resourceService;

    @BeforeAll
    static void initMybatisPlus() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, ResourceRecord.class);
        TableInfoHelper.initTableInfo(assistant, ResourceFlow.class);
        TableInfoHelper.initTableInfo(assistant, ResourceRequest.class);
        TableInfoHelper.initTableInfo(assistant, ResourceDeductDetail.class);
    }

    @BeforeEach
    void setUp() throws InterruptedException {
        lenient().when(redissonClient.getLock(anyString())).thenReturn(lock);
        lenient().when(lock.tryLock(anyLong(), any(TimeUnit.class))).thenReturn(true);
        lenient().when(lock.isHeldByCurrentThread()).thenReturn(true);
        lenient().doAnswer(invocation -> {
            ResourceRequest request = invocation.getArgument(0);
            if (request.getId() == null) {
                request.setId(1L);
            }
            return 1;
        }).when(resourceRequestMapper).insert(any(ResourceRequest.class));
        lenient().doAnswer(invocation -> {
            ResourceFlow flow = invocation.getArgument(0);
            if (flow.getId() == null) {
                flow.setId(10L);
            }
            return 1;
        }).when(resourceFlowMapper).insert(any(ResourceFlow.class));
    }

    @Test
    void grantResource_whenRequestAlreadySucceeded_skipsDuplicateHandling() {
        ResourceRequest existing = new ResourceRequest();
        existing.setStatus(RequestStatus.SUCCESS.getCode());
        when(resourceRequestMapper.selectOne(any())).thenReturn(existing);

        var request = new com.iflytek.astron.console.hub.dto.point.ResourceGrantRequest();
        request.setUid("u1");
        request.setResourceType("POINT");
        request.setSourceType("MEMBER");
        request.setAmount(10L);
        request.setExpireTime(LocalDateTime.now().plusDays(30));
        request.setRequestId("req-1");
        request.setRequestType("AGENT");

        resourceService.grantResource(request);

        verify(resourceRequestMapper, never()).insert(any(ResourceRequest.class));
        verify(resourceRecordMapper, never()).insert(any(ResourceRecord.class));
        verify(resourceFlowMapper, never()).insert(any(ResourceFlow.class));
    }

    @Test
    void deductResource_whenBalanceInsufficient_deductsOnlyAvailableAmount() {
        when(resourceRequestMapper.selectOne(any())).thenReturn(null);
        when(resourceRecordMapper.update(isNull(), any())).thenReturn(1);

        ResourceRecord activityRecord = new ResourceRecord();
        activityRecord.setId(1L);
        activityRecord.setUid("u1");
        activityRecord.setResourceType("POINT");
        activityRecord.setSourceType(SourceType.ACTIVITY.getCode());
        activityRecord.setTotalAmount(5L);
        activityRecord.setUsedAmount(0L);
        activityRecord.setRemainAmount(5L);
        activityRecord.setStatus(ResourceStatus.VALID.getCode());
        activityRecord.setVersion(0L);
        activityRecord.setExpireTime(LocalDateTime.now().plusDays(1));

        ResourceRecord buyRecord = new ResourceRecord();
        buyRecord.setId(2L);
        buyRecord.setUid("u1");
        buyRecord.setResourceType("POINT");
        buyRecord.setSourceType(SourceType.BUY.getCode());
        buyRecord.setTotalAmount(10L);
        buyRecord.setUsedAmount(0L);
        buyRecord.setRemainAmount(10L);
        buyRecord.setStatus(ResourceStatus.VALID.getCode());
        buyRecord.setVersion(0L);
        buyRecord.setExpireTime(LocalDateTime.now().plusDays(10));

        when(resourceRecordMapper.selectList(any())).thenReturn(new ArrayList<>(List.of(activityRecord, buyRecord)));

        ResourceDeductRequest request = new ResourceDeductRequest();
        request.setUid("u1");
        request.setRequestId("req-deduct");
        request.setResourceType("POINT");
        request.setRequestType("AGENT");
        request.setAmount(20L);
        request.setReason("consume");

        resourceService.deductResource(request);

        ArgumentCaptor<ResourceFlow> flowCaptor = ArgumentCaptor.forClass(ResourceFlow.class);
        verify(resourceFlowMapper).insert(flowCaptor.capture());
        assertThat(flowCaptor.getValue().getDirection()).isEqualTo(Direction.DEDUCT.getCode());
        assertThat(flowCaptor.getValue().getAmount()).isEqualTo(15L);

        ArgumentCaptor<ResourceDeductDetail> detailCaptor = ArgumentCaptor.forClass(ResourceDeductDetail.class);
        verify(resourceDeductDetailMapper, org.mockito.Mockito.times(2)).insert(detailCaptor.capture());
        assertThat(detailCaptor.getAllValues()).extracting(ResourceDeductDetail::getAmount).containsExactly(5L, 10L);
    }

    @Test
    void invalidateMemberResource_whenSourceTypeMissing_defaultsToMember() {
        when(resourceRequestMapper.selectOne(any())).thenReturn(null);
        when(resourceRecordMapper.update(isNull(), any())).thenReturn(1);

        ResourceRecord memberRecord = new ResourceRecord();
        memberRecord.setId(3L);
        memberRecord.setUid("u1");
        memberRecord.setResourceType("POINT");
        memberRecord.setSourceType(SourceType.MEMBER.getCode());
        memberRecord.setRemainAmount(8L);
        memberRecord.setStatus(ResourceStatus.VALID.getCode());
        when(resourceRecordMapper.selectList(any())).thenReturn(new ArrayList<>(List.of(memberRecord)));

        ResourceInvalidateMemberRequest request = new ResourceInvalidateMemberRequest();
        request.setUid("u1");
        request.setResourceType("POINT");
        request.setRequestId("req-invalidate");
        request.setRequestType("AGENT");
        request.setReason("expire membership");

        resourceService.invalidateMemberResource(request);

        ArgumentCaptor<ResourceFlow> flowCaptor = ArgumentCaptor.forClass(ResourceFlow.class);
        verify(resourceFlowMapper).insert(flowCaptor.capture());
        assertThat(flowCaptor.getValue().getDirection()).isEqualTo(Direction.FAILURE.getCode());
        assertThat(flowCaptor.getValue().getAmount()).isEqualTo(8L);
        assertThat(flowCaptor.getValue().getVisible()).isZero();
    }

    @Test
    void refundResource_whenOriginalDeductFlowMissing_throwsBusinessException() {
        when(resourceFlowMapper.selectOne(any())).thenReturn(null);

        ResourceRefundRequest request = new ResourceRefundRequest();
        request.setRequestId("refund-1");
        request.setRequestType("AGENT");
        request.setOriginalRequestId("missing-deduct");

        assertThatThrownBy(() -> resourceService.refundResource(request))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(ResponseEnum.RESOURCE_ORIGINAL_DEDUCT_NOT_FOUND.getCode());
    }
}
