package org.shuzhi.Config;

import cn.dev33.satoken.stp.StpUtil;

public class UserContext {
    public static String getUserId() {
        // 1. 首先尝试从ReactiveContext获取
        String id = ReactiveContext.getUserId();
       return id;
    }

    /**
     * 清理用户上下文信息，避免ThreadLocal内存泄漏
     * 建议在请求处理完成后调用此方法
     */
    public static void cleanup() {
        ReactiveContext.clear();
    }
}
