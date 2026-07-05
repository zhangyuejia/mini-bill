package com.minibill.bus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.bus.entity.BusMessage;

/**
 * 消息通知服务接口
 */
public interface MessageService {

    /** 分页查询消息 */
    Page<BusMessage> pageMessage(Integer pageNum, Integer pageSize, Long userId, String type, Integer isRead);

    /** 标记单条已读 */
    void markAsRead(Long id, Long userId);

    /** 全部标为已读 */
    void markAllAsRead(Long userId);

    /** 删除消息 */
    void deleteMessage(Long id, Long userId);

    /** 获取未读数量 */
    long getUnreadCount(Long userId);

    /** 创建消息（系统内部调用） */
    void createMessage(BusMessage message);
}
