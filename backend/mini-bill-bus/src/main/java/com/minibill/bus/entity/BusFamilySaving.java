package com.minibill.bus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minibill.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 家庭储蓄
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bus_family_saving")
public class BusFamilySaving extends BaseEntity {

    private Long familyId;
    private LocalDate savingDate;  // 储蓄日期
    private BigDecimal totalAmount; // 合计
}
