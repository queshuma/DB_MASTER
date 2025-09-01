package org.shuzhi.Common;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.shuzhi.Config.ReactiveContext;
import org.shuzhi.Config.UserContext;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Aspect
@Component
public class ToolContextAspect {
    // 拦截所有 @Tool 注解的方法
    @Around("@annotation(org.springframework.ai.tool.annotation.Tool)")
    public Object injectUserId(ProceedingJoinPoint pjp) throws Throwable {
        try {

            // 执行原方法
            Object result = pjp.proceed();

            // 如果返回的是Mono或Flux，我们需要确保上下文在异步操作中保持
            if (result instanceof Mono) {
                return ((Mono<?>) result).contextWrite(ctx -> {
                    if (!ctx.hasKey("userId")) {
                        ctx = ctx.put("userId", ReactiveContext.getUserId());
                    }
                    return ctx;
                });
            }

            return result;
        } finally {
            // 在响应式环境中，我们不在这里清理上下文
            // 而是在整个请求完成时清理
        }
    }
}
