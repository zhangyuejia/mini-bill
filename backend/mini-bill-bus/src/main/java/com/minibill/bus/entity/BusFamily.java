package com.minibill.bus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minibill.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 家庭
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bus_family")
public class BusFamily extends BaseEntity {

    private String name;
    private Long ownerId;      // 一家之主
    private Integer defaultFlag; // 0-非默认 1-默认
    private Integer status;    // 0-正常 1-解散

    // 非数据库字段
    private transient String ownerName;
}
