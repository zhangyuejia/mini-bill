package com.minibill.bus.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@TableName("bus_education_item")
public class BusEducationItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long educationId;
    private LocalDate costDate;
    private String itemType;
    private BigDecimal amount;
    private String remark;

    @TableField(exist = false)
    private String itemTypeText;

    @TableField(exist = false)
    private List<BusAttachment> attachments;
}
