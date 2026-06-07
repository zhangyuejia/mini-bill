package com.minibill.bus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 储蓄记录
 */
@Data
@TableName("bus_saving_record")
public class BusSavingRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long savingId;     // 对应bus_family_saving的id
    private Long savingItemId; // 储蓄项ID
    private Long memberId;     // 家庭成员
    private BigDecimal amount; // 金额
}
