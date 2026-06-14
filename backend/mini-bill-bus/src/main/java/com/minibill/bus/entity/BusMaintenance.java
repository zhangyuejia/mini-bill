package com.minibill.bus.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.minibill.common.annotation.DictText;
import com.minibill.common.constant.DictCode;
import com.minibill.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 维护费用
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bus_maintenance")
public class BusMaintenance extends BaseEntity {

    private Long addressId;
    @DictText(dictCode = DictCode.MAINTENANCE_TYPE)
    private String type;
    /** 字典文本：type 对应的中文（非数据库字段，由 DictAspect 自动填充） */
    private transient String typeText;
    private LocalDate costDate;
    private BigDecimal cost;
    private String remark;

    @TableField(exist = false)
    private String addressName;

    @TableField(exist = false)
    private List<BusAttachment> attachments;
}
