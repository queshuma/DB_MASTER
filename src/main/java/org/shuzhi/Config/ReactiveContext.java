package org.shuzhi.Config;

import com.alibaba.ttl.TransmittableThreadLocal;

public class ReactiveContext {
    // 使用 InheritableThreadLocal 支持线程继承
    private static final TransmittableThreadLocal<String> userId = new TransmittableThreadLocal<>();

    public static void setUserId(String id) {
        userId.set(id);
    }

    public static String getUserId() {
        return userId.get();
    }

    public static void clear() {
        userId.remove();
    }
}
