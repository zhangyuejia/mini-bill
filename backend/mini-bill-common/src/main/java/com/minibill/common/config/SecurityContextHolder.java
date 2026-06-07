package com.minibill.common.config;

import com.minibill.common.constant.Constants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 安全上下文持有者（使用ThreadLocal存储当前用户信息）
 */
public class SecurityContextHolder {

    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = ThreadLocal.withInitial(ConcurrentHashMap::new);

    public static void setUserId(Long userId) {
        THREAD_LOCAL.get().put(Constants.CACHE_KEY_USER + "id", userId);
    }

    public static Long getUserId() {
        Object val = THREAD_LOCAL.get().get(Constants.CACHE_KEY_USER + "id");
        return val instanceof Long ? (Long) val : null;
    }

    public static void set(String key, Object value) {
        THREAD_LOCAL.get().put(key, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) THREAD_LOCAL.get().get(key);
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }
}
