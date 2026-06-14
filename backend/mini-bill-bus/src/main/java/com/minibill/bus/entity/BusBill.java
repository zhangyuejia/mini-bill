package com.minibill.bus.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.minibill.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 房租水电账单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bus_bill")
public class BusBill extends BaseEntity {

    private Long familyId;
    private Long addressId;
    private Integer period;      // 账期 如202605表示2026年05月

    // 房租
    private BigDecimal rent;

    // 水费
    private BigDecimal waterPrevReading;  // 上月表底
    private BigDecimal waterCurrReading;  // 本月表底
    private BigDecimal waterUnitPrice;    // 单价
    private BigDecimal waterAmount;       // 金额

    // 电费
    private BigDecimal electricPrevReading;
    private BigDecimal electricCurrReading;
    private BigDecimal electricUnitPrice;
    private BigDecimal electricAmount;

    // 管理费
    private BigDecimal managementFee;

    // 其他费用
    private BigDecimal otherFee;

    // 抹零金额
    private BigDecimal roundingAmount;

    // 合计
    private BigDecimal totalAmount;

    private String remark;

    @TableField(exist = false)
    private List<BusAttachment> attachments;
}
