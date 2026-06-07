package com.minibill.bus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minibill.common.annotation.DictText;
import com.minibill.common.constant.DictCode;
import com.minibill.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 物件
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bus_item")
public class BusItem extends BaseEntity {

    private Long familyId;
    private Long addressId;
    private String name;
    @DictText(dictCode = DictCode.ITEM_TYPE)
    private String type;         // 类型（字典编码 item_type）

    /** 字典文本：type 对应的中文（非数据库字段，由 DictAspect 自动填充） */
    private transient String typeText;

    private BigDecimal purchaseAmount;
    private LocalDate purchaseDate;
    private String remark;
}
