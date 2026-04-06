package com.ruoyi.taskmgt.service.impl;

import com.ruoyi.app.domain.TAppConstraint;
import com.ruoyi.app.domain.TAppLibrary;
import com.ruoyi.app.service.ITAppConstraintService;
import com.ruoyi.app.service.ITAppLibraryService;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.task.TaskmgtException;
import com.ruoyi.common.threadlocal.TenantContext;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.robots.domain.RobotGroups;
import com.ruoyi.robots.service.IRobotGroupsService;
import com.ruoyi.taskmgt.domain.TemplateRepository;
import com.ruoyi.taskmgt.domain.bo.Template;
import com.ruoyi.taskmgt.service.vo.TemplateVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.MessageSourceAccessor;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemplateServiceImplTest {

    @Mock private TemplateRepository templateRepository;
    @Mock private MessageSourceAccessor messageSourceAccessor;
    @Mock private RedisCache redisUtil;
    @Mock private IRobotGroupsService robotGroupsService;
    @Mock private ITAppLibraryService appLibraryService;
    @Mock private ITAppConstraintService appConstraintService;

    @InjectMocks
    private TemplateServiceImpl templateService;

    private static final Long TEMPLATE_ID = 100L;
    private static final Long APP_ID = 200L;
    private static final Long GROUP_ID = 300L;
    private static final Long TENANT_ID = 1L;

    private Template createDefaultTemplate() {
        Template template = new Template();
        template.setId(TEMPLATE_ID);
        template.setName("测试模板");
        template.setAppId(APP_ID);
        template.setStatus(Template.ENABLED);
        template.setRobotGroupIds(List.of(GROUP_ID));
        template.setRules(new ArrayList<>());
        template.setRuleIds(new ArrayList<>());
        return template;
    }

    private TAppLibrary createDefaultApp() {
        TAppLibrary app = new TAppLibrary();
        app.setId(APP_ID);
        app.setAppName("测试应用");
        return app;
    }

    private RobotGroups createDefaultGroup() {
        RobotGroups group = new RobotGroups();
        group.setId(GROUP_ID);
        group.setName("测试组");
        return group;
    }

    // ==================== createTemplate ====================
    @Test
    void testCreateTemplate_Success() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Template template = createDefaultTemplate();
            TAppLibrary app = createDefaultApp();
            when(appLibraryService.selectTAppLibraryById(APP_ID)).thenReturn(app);
            when(templateRepository.insert(any(Template.class))).thenAnswer(inv -> {
                Template arg = inv.getArgument(0);
                arg.setId(TEMPLATE_ID);
                return arg;
            });

            TemplateVo result = templateService.createTemplate(template);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(TEMPLATE_ID);
            verify(templateRepository).insert(any(Template.class));
        }
    }

    @Test
    void testCreateTemplate_WithRules_Success() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Template template = createDefaultTemplate();
            TAppConstraint rule = new TAppConstraint();
            rule.setId(1L);
            template.setRules(List.of(rule));

            TAppLibrary app = createDefaultApp();
            when(appLibraryService.selectTAppLibraryById(APP_ID)).thenReturn(app);
            when(appConstraintService.insertTAppConstraint(any())).thenReturn(1);
            when(appConstraintService.selectTAppConstraintList(any())).thenReturn(List.of(rule));
            when(templateRepository.insert(any(Template.class))).thenAnswer(inv -> inv.getArgument(0));

            templateService.createTemplate(template);

            verify(appConstraintService).insertTAppConstraint(any());
            ArgumentCaptor<Template> captor = ArgumentCaptor.forClass(Template.class);
            verify(templateRepository).insert(captor.capture());
            assertThat(captor.getValue().getRuleIds()).contains(1L);
        }
    }

    // ==================== updateTemplate ====================
    @Test
    void testUpdateTemplate_Success() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Template existing = createDefaultTemplate();
            existing.setStatus(Template.ENABLED);
            when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(existing));

            Template update = createDefaultTemplate();
            update.setName("更新后模板");
            when(templateRepository.update(any(Template.class))).thenReturn(List.of("template:key"));

            templateService.updateTemplate(update);

            verify(templateRepository).update(any(Template.class));
            verify(redisUtil).deleteObject(anyList());
        }
    }

    @Test
    void testUpdateTemplate_TemplateNotFound_ThrowsException() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> templateService.updateTemplate(createDefaultTemplate()))
                    .isInstanceOf(TaskmgtException.class);
        }
    }

    @Test
    void testUpdateTemplate_InvalidStatus_ThrowsException() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Template existing = createDefaultTemplate();
            existing.setStatus(Template.DELETED);
            when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(existing));

            assertThatThrownBy(() -> templateService.updateTemplate(createDefaultTemplate()))
                    .isInstanceOf(TaskmgtException.class);
        }
    }

    // ==================== deleteTemplate ====================
    @Test
    void testDeleteTemplate_Success() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Template template = createDefaultTemplate();
            template.setStatus(Template.DISABLED);
            template.setRuleIds(List.of(1L, 2L));
            when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
            when(templateRepository.update(any(Template.class))).thenReturn(List.of("template:key"));

            templateService.deleteTemplate(TEMPLATE_ID);

            verify(appConstraintService).deleteTAppConstraintById(1L);
            verify(appConstraintService).deleteTAppConstraintById(2L);
            ArgumentCaptor<Template> captor = ArgumentCaptor.forClass(Template.class);
            verify(templateRepository).update(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(Template.DELETED);
        }
    }

    @Test
    void testDeleteTemplate_NotDisabled_ThrowsException() {
        Template template = createDefaultTemplate();
        template.setStatus(Template.ENABLED);
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));

        assertThatThrownBy(() -> templateService.deleteTemplate(TEMPLATE_ID))
                .isInstanceOf(TaskmgtException.class);
    }

    // ==================== banTemplate & resumeTemplate ====================
    @Test
    void testBanTemplate_Success() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Template template = createDefaultTemplate();
            template.setStatus(Template.ENABLED);
            when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
            when(templateRepository.update(any(Template.class))).thenReturn(List.of("template:key"));

            templateService.banTemplate(TEMPLATE_ID);

            ArgumentCaptor<Template> captor = ArgumentCaptor.forClass(Template.class);
            verify(templateRepository).update(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(Template.DISABLED);
        }
    }

    @Test
    void testResumeTemplate_Success() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Template template = createDefaultTemplate();
            template.setStatus(Template.DISABLED);
            when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
            when(templateRepository.update(any(Template.class))).thenReturn(List.of("template:key"));

            templateService.resumeTemplate(TEMPLATE_ID);

            ArgumentCaptor<Template> captor = ArgumentCaptor.forClass(Template.class);
            verify(templateRepository).update(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(Template.ENABLED);
        }
    }

    @Test
    void testResumeTemplate_NotDisabled_ThrowsException() {
        Template template = createDefaultTemplate();
        template.setStatus(Template.ENABLED);
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));

        assertThatThrownBy(() -> templateService.resumeTemplate(TEMPLATE_ID))
                .isInstanceOf(TaskmgtException.class);
    }

    // ==================== retrieveTemplates ====================
    @Test
    void testRetrieveTemplates_Success() {
        try (var tenantMock = mockStatic(TenantContext.class);
             var securityMock = mockStatic(SecurityUtils.class)) {
            tenantMock.when(TenantContext::get).thenReturn(TENANT_ID);
            securityMock.when(() -> SecurityUtils.isAdmin(anyLong())).thenReturn(false);

            Template template = createDefaultTemplate();
            when(templateRepository.getTemplates(any(), any(), any(), any(), any())).thenReturn(List.of(template));
            when(robotGroupsService.selectRobotGroupsById(GROUP_ID)).thenReturn(createDefaultGroup());
            when(appLibraryService.selectTAppLibraryById(APP_ID)).thenReturn(createDefaultApp());

            List<TemplateVo> result = templateService.retrieveTemplates(null, null, null, null);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getAppName()).isEqualTo("测试应用");
            assertThat(result.get(0).getRobotGroupNames()).contains("测试组");
        }
    }

    // ==================== getTemplate ====================
    @Test
    void testGetTemplate_Success() {
        Template template = createDefaultTemplate();
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.of(template));
        when(robotGroupsService.selectRobotGroupsById(GROUP_ID)).thenReturn(createDefaultGroup());
        when(appLibraryService.selectTAppLibraryById(APP_ID)).thenReturn(createDefaultApp());

        TemplateVo result = templateService.getTemplate(TEMPLATE_ID);

        assertThat(result).isNotNull();
        assertThat(result.getAppName()).isEqualTo("测试应用");
        assertThat(result.getRobotGroupNames()).contains("测试组");
    }

    @Test
    void testGetTemplate_NotFound_ThrowsException() {
        when(templateRepository.findById(TEMPLATE_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> templateService.getTemplate(TEMPLATE_ID))
                .isInstanceOf(TaskmgtException.class);
    }
}