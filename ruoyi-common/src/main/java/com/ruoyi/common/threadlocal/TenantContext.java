package com.ruoyi.common.threadlocal;

public class TenantContext {
    private static final ThreadLocal<Long> TENANT_LOCAL = new ThreadLocal<>();

    /**
     * 设置当前租户ID
     */
    public static void set(Long tenantId) {
        TENANT_LOCAL.set(tenantId);
    }

    /**
     * 获取当前租户ID
     */
    public static Long get() {
        return TENANT_LOCAL.get();
    }

    /**
     * 清除租户信息（防止内存泄漏）
     */
    public static void clear() {
        TENANT_LOCAL.remove();
    }
}
