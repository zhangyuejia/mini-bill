package com.minibill.bus.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.minibill.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bus_education")
public class BusEducation extends BaseEntity {

    private Long memberId;
    private LocalDate semesterDate;
    private BigDecimal tuition;
    private BigDecimal mealFee;
    private BigDecimal accommodationFee;
    private String remark;

    @TableField(exist = false)
    private String memberName;

    @TableField(exist = false)
    private List<BusEducationItem> items;

    @TableField(exist = false)
    private List<BusAttachment> attachments;
}
