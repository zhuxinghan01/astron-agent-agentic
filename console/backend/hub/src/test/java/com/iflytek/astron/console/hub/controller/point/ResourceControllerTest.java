package com.iflytek.astron.console.hub.controller.point;

import com.iflytek.astron.console.commons.config.JwtClaimsFilter;
import com.iflytek.astron.console.hub.dto.point.ResourceDeductRequest;
import com.iflytek.astron.console.hub.dto.point.ResourceGrantRequest;
import com.iflytek.astron.console.hub.service.point.ResourceService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ResourceControllerTest {

    @Mock
    private ResourceService resourceService;

    @InjectMocks
    private ResourceController resourceController;

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void grantResource_injectsCurrentUidIntoRequest() {
        bindUid("current-user");

        ResourceGrantRequest request = new ResourceGrantRequest();
        request.setResourceType("POINT");
        request.setSourceType("MEMBER");
        request.setAmount(10L);
        request.setExpireTime(LocalDateTime.now().plusDays(1));
        request.setRequestId("req-1");
        request.setRequestType("AGENT");

        resourceController.grantResource(request);

        ArgumentCaptor<ResourceGrantRequest> captor = ArgumentCaptor.forClass(ResourceGrantRequest.class);
        verify(resourceService).grantResource(captor.capture());
        assertThat(captor.getValue().getUid()).isEqualTo("current-user");
    }

    @Test
    void deductResource_injectsCurrentUidIntoRequest() {
        bindUid("current-user");

        ResourceDeductRequest request = new ResourceDeductRequest();
        request.setResourceType("POINT");
        request.setRequestId("req-2");
        request.setRequestType("AGENT");
        request.setAmount(3L);

        resourceController.deductResource(request);

        ArgumentCaptor<ResourceDeductRequest> captor = ArgumentCaptor.forClass(ResourceDeductRequest.class);
        verify(resourceService).deductResource(captor.capture());
        assertThat(captor.getValue().getUid()).isEqualTo("current-user");
    }

    private void bindUid(String uid) {
        HttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(JwtClaimsFilter.USER_ID_ATTRIBUTE, uid);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes((MockHttpServletRequest) request));
    }
}
