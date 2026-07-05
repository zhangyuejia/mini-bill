package com.minibill.bus.controller;

import com.minibill.bus.service.SseEmitterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE消息推送控制器
 */
@Tag(name = "SSE消息推送")
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class SseController {

    private final SseEmitterService sseEmitterService;

    @Operation(summary = "订阅SSE消息推送")
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@RequestHeader("X-User-Id") Long userId) {
        return sseEmitterService.createEmitter(userId);
    }
}
