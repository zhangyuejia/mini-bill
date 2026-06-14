package com.minibill.bus.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 物件分页查询参数
 */
@Data
public class ItemPageQuery {

    /** 当前页 */
    private Integer pageNum = 1;

    /** 每页条数 */
    private Integer pageSize = 10;

    /** 家庭ID（必填） */
    private Long familyId;

    /** 住址ID */
    private Long addressId;

    /** 名称（模糊） */
    private String name;

    /** 类型（精确） */
    private String type;

    /** 购买金额起 */
    private BigDecimal purchaseAmountStart;

    /** 购买金额止 */
    private BigDecimal purchaseAmountEnd;

    /** 购买日期起 */
    private LocalDate purchaseDateStart;

    /** 购买日期止 */
    private LocalDate purchaseDateEnd;
}
