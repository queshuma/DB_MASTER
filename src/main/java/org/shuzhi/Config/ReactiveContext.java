package org.shuzhi.Config;

public class ReactiveContext {
    // 使用 InheritableThreadLocal 支持线程继承
    private static final ThreadLocal<String> userId = new InheritableThreadLocal<>();

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
