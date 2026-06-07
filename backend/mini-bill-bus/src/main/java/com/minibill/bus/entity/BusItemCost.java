package com.minibill.bus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minibill.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 物件费用
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bus_item_cost")
public class BusItemCost extends BaseEntity {

    private Long familyId;
    private Long addressId;
    private Long itemId;
    private LocalDate costDate;
    private BigDecimal mileage;     // 里程（交通工具时填写）
    private BigDecimal cost;        // 费用
    private String remark;
}
