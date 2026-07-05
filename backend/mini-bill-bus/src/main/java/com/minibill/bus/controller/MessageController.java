package com.minibill.bus.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.common.result.Result;
import com.minibill.bus.entity.BusMessage;
import com.minibill.bus.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 消息通知控制器
 */
@Tag(name = "消息通知")
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "分页查询消息")
    @GetMapping("/page")
    public Result<Page<BusMessage>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @RequestHeader("X-User-Id") Long userId,
                                         @RequestParam(required = false) String type,
                                         @RequestParam(required = false) Integer isRead) {
        return Result.success(messageService.pageMessage(pageNum, pageSize, userId, type, isRead));
    }

    @Operation(summary = "获取未读消息数量")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(messageService.getUnreadCount(userId));
    }

    @Operation(summary = "标记单条消息为已读")
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id,
                                   @RequestHeader("X-User-Id") Long userId) {
        messageService.markAsRead(id, userId);
        return Result.success();
    }

    @Operation(summary = "全部标为已读")
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(@RequestHeader("X-User-Id") Long userId) {
        messageService.markAllAsRead(userId);
        return Result.success();
    }

    @Operation(summary = "删除消息")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestHeader("X-User-Id") Long userId) {
        messageService.deleteMessage(id, userId);
        return Result.success();
    }
}
