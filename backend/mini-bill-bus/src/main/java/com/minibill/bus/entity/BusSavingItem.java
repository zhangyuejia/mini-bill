package com.minibill.bus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minibill.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 储蓄项
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bus_saving_item")
public class BusSavingItem extends BaseEntity {

    private Long familyId;
    private Long memberId;   // 家庭成员（用户ID）
    private String name;     // 储蓄项名称
    private Integer status;  // 0-正常 1-停用
}
