package com.minibill.bus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minibill.common.entity.BaseEntity;

import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 住址
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bus_address")
public class BusAddress extends BaseEntity {

    private Long familyId;
    private String name;       // 住址名称（如：老家、租房地）
    private String province;   // 省
    private String city;       // 市
    private String district;   // 区
    private String streetNumber; // 门牌号
    private String addressImage; // 地址图片URL
    private Integer status;    // 0-正常 1-删除

    /** 默认房租 */
    private BigDecimal defaultRent;
    /** 电费默认单价 */
    private BigDecimal defaultElectricPrice;
    /** 水费默认单价 */
    private BigDecimal defaultWaterPrice;
    /** 默认管理费 */
    private BigDecimal defaultManagementFee;
}
