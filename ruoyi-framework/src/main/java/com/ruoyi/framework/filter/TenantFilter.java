//package com.ruoyi.framework.filter;
//
//import com.ruoyi.common.core.domain.model.LoginUser;
//import com.ruoyi.common.threadlocal.TenantContext;
//import com.ruoyi.framework.web.service.TokenService;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//
//@Component
//public class TenantFilter extends OncePerRequestFilter {
//
//    private static final Logger log = LoggerFactory.getLogger(TenantFilter.class);
//
//    @Autowired
//    private TokenService tokenService;
//
//    public TenantFilter() {
//        System.out.println("=== [租户调试] TenantFilter 已加载 ===");
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//
//        String uri = request.getRequestURI();
//
//        try {
//            LoginUser loginUser = tokenService.getLoginUser(request);
//
//            if (loginUser != null && loginUser.getUser() != null) {
//                Long tenantId = loginUser.getUser().getTenantId();
//                Long userId = loginUser.getUserId();
//
//                // 只对需要租户过滤的请求输出日志
//                if (uri.contains("/robots/robots/list") || uri.contains("/mode/robots/list")) {
//                    System.out.println("=== [租户调试] TenantFilter - URI: " + uri);
//                    System.out.println("=== [租户调试] TenantFilter - userId: " + userId);
//                    System.out.println("=== [租户调试] TenantFilter - tenantId from user: " + tenantId);
//                }
//
//                if (tenantId != null && tenantId != 0) {
//                    TenantContext.set(tenantId);
//                    if (uri.contains("/robots/robots/list") || uri.contains("/mode/robots/list")) {
//                        System.out.println("=== [租户调试] TenantFilter - 设置租户上下文为: " + tenantId);
//                    }
//                } else {
//                    System.out.println("=== [租户调试] TenantFilter - 警告：tenantId为null或0，使用默认值0");
//                    TenantContext.set(0L);
//                }
//            }
//        } catch (Exception e) {
//            System.out.println("=== [租户调试] TenantFilter - 异常: " + e.getMessage());
//        }
//
//        try {
//            filterChain.doFilter(request, response);
//        } finally {
//            // 注意：不要在 finally 中清除 TenantContext
//            // 让请求自然结束，避免在业务处理前被清除
//        }
//    }
//}