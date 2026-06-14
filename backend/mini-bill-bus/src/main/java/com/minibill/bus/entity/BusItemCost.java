package com.minibill.bus.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.minibill.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 物件费用
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bus_item_cost")
public class BusItemCost extends BaseEntity {

    private Long itemId;
    private LocalDate costDate;
    private BigDecimal mileage;     // 里程（交通工具时填写）
    private BigDecimal cost;        // 费用
    private String remark;

    @TableField(exist = false)
    private Long addressId;

    @TableField(exist = false)
    private String itemName;

    @TableField(exist = false)
    private String addressName;

    @TableField(exist = false)
    private List<BusAttachment> attachments;
}
