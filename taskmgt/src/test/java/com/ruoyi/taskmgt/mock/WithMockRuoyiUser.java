package com.ruoyi.taskmgt.mock;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockRuoyiUserSecurityContextFactory.class)
public @interface WithMockRuoyiUser {
    String username() default "admin";
    String userId() default "1";
    String[] authorities() default {"*:*:*"};
    String deptId() default "103";
}