package com.minibill.bus.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * SSE连接管理服务
 * 管理所有用户的SSE长连接，支持向指定用户推送消息
 */
@Slf4j
@Service
public class SseEmitterService {

    /** 用户ID → SseEmitter列表（一个用户可能有多个设备/标签页） */
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    /** SseEmitter超时时间：35分钟 */
    private static final long EMITTER_TIMEOUT = TimeUnit.MINUTES.toMillis(35);

    /**
     * 为用户创建新的SSE连接
     */
    public SseEmitter createEmitter(Long userId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);

        userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        // 连接建立时发送一条初始事件确认连接
        try {
            emitter.send(SseEmitter.event().name("connected").data("SSE连接已建立"));
        } catch (IOException e) {
            log.warn("SSE初始事件发送失败 userId={}", userId);
        }

        // 清理回调
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));

        log.debug("SSE连接建立 userId={}, 当前连接数={}", userId, getUserEmitterCount(userId));
        return emitter;
    }

    /**
     * 向指定用户推送消息事件
     */
    public void sendToUser(Long userId, String eventName, Object data) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters == null || emitters.isEmpty()) {
            log.debug("用户无活跃SSE连接 userId={}", userId);
            return;
        }

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (IOException e) {
                log.debug("SSE推送失败，移除失效连接 userId={}", userId);
                emitters.remove(emitter);
            }
        }

        // 清理空列表
        if (emitters.isEmpty()) {
            userEmitters.remove(userId);
        }
    }

    /**
     * 移除失效的连接
     */
    private void removeEmitter(Long userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                userEmitters.remove(userId);
            }
        }
        log.debug("SSE连接关闭 userId={}, 剩余连接数={}", userId, getUserEmitterCount(userId));
    }

    /**
     * 获取用户的活跃连接数
     */
    private int getUserEmitterCount(Long userId) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.get(userId);
        return emitters != null ? emitters.size() : 0;
    }

    /**
     * 心跳：每25秒向所有连接发送心跳，防止代理/负载均衡超时断开
     */
    @Scheduled(fixedRate = 25000)
    public void sendHeartbeat() {
        userEmitters.forEach((userId, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().comment("ping"));
                } catch (IOException e) {
                    emitters.remove(emitter);
                }
            }
            if (emitters.isEmpty()) {
                userEmitters.remove(userId);
            }
        });
    }
}
