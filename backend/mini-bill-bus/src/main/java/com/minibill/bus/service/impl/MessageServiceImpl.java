package com.minibill.bus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.common.exception.BusinessException;
import com.minibill.bus.entity.BusMessage;
import com.minibill.bus.mapper.BusMessageMapper;
import com.minibill.bus.service.MessageService;
import com.minibill.bus.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息通知服务实现
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final BusMessageMapper messageMapper;
    private final SseEmitterService sseEmitterService;

    @Override
    public Page<BusMessage> pageMessage(Integer pageNum, Integer pageSize, Long userId, String type, Integer isRead) {
        Page<BusMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BusMessage> wrapper = new LambdaQueryWrapper<BusMessage>()
                .eq(BusMessage::getUserId, userId)
                .eq(type != null && !type.isBlank(), BusMessage::getType, type)
                .eq(isRead != null, BusMessage::getIsRead, isRead)
                .orderByDesc(BusMessage::getCreateTime);
        return messageMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long id, Long userId) {
        BusMessage message = messageMapper.selectById(id);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        if (!message.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此消息");
        }
        if (message.getIsRead() == 0) {
            message.setIsRead(1);
            messageMapper.updateById(message);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        LambdaUpdateWrapper<BusMessage> wrapper = new LambdaUpdateWrapper<BusMessage>()
                .eq(BusMessage::getUserId, userId)
                .eq(BusMessage::getIsRead, 0)
                .set(BusMessage::getIsRead, 1);
        messageMapper.update(null, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long id, Long userId) {
        BusMessage message = messageMapper.selectById(id);
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        if (!message.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此消息");
        }
        messageMapper.deleteById(id);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return messageMapper.selectCount(new LambdaQueryWrapper<BusMessage>()
                .eq(BusMessage::getUserId, userId)
                .eq(BusMessage::getIsRead, 0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createMessage(BusMessage message) {
        messageMapper.insert(message);

        // 通过SSE实时推送
        try {
            Map<String, Object> eventData = new HashMap<>();
            eventData.put("id", message.getId());
            eventData.put("title", message.getTitle());
            eventData.put("type", message.getType());
            eventData.put("createTime", message.getCreateTime() != null
                    ? message.getCreateTime().toString() : null);
            eventData.put("unreadCount", getUnreadCount(message.getUserId()));

            sseEmitterService.sendToUser(message.getUserId(), "message", eventData);
        } catch (Exception e) {
            // SSE推送失败不影响消息入库
        }
    }
}
