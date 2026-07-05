package com.minibill.bus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息通知
 */
@Data
@TableName("bus_message")
public class BusMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 消息标题 */
    private String title;

    /** 消息内容 */
    private String content;

    /** 消息类型：bill_inspection-账单校验 */
    private String type;

    /** 是否已读 0-未读 1-已读 */
    private Integer isRead;

    /** 接收用户ID */
    private Long userId;

    /** 关联家庭ID */
    private Long familyId;

    /** 关联账单ID */
    private Long relatedBillId;

    /** 创建时间 */
    private LocalDateTime createTime;
}
