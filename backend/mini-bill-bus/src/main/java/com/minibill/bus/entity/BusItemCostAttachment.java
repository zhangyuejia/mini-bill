package com.minibill.bus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 物件费用附件
 */
@Data
@TableName("bus_item_cost_attachment")
public class BusItemCostAttachment implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long costId;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
}
