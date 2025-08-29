package org.shuzhi.Common;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.shuzhi.Config.ReactiveContext;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ToolContextAspect {
    // 拦截所有 @Tool 注解的方法
    @Around("@annotation(org.springframework.ai.tool.annotation.Tool)")
    public Object injectUserId(ProceedingJoinPoint pjp) throws Throwable {
        // 检查是否已经设置了userId
        String existingUserId = ReactiveContext.getUserId();
        boolean isUserIdSet = existingUserId != null;
        
        try {
            // 如果未设置userId，则尝试从StpUtil获取并设置
            if (!isUserIdSet) {
                try {
                    // 尝试获取当前登录用户ID
                    String userId = StpUtil.getLoginIdAsString();
                    if (userId != null && !userId.isEmpty()) {
                        ReactiveContext.setUserId(userId);
                    } else {
                        // 如果获取不到，设置为匿名用户
                        ReactiveContext.setUserId("anonymous");
                    }
                } catch (Exception e) {
                    // 如果获取登录用户信息失败，设置为匿名用户
                    ReactiveContext.setUserId("anonymous");
                }
            }
            
            // 执行原方法
            return pjp.proceed();
        } finally {
            // 只有当当前切面设置了userId时才清理，避免过早清理上层设置的userId
            if (!isUserIdSet) {
                ReactiveContext.clear();
            }
        }
    }
}
