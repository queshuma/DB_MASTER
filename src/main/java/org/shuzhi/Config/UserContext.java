package org.shuzhi.Config;

import cn.dev33.satoken.stp.StpUtil;

public class UserContext {
    public static String getUserId() {
        String id = ReactiveContext.getUserId();
        return id != null ? id : StpUtil.getLoginIdAsString();
    }
}
