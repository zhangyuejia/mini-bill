package com.minibill.bus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 物件附件
 */
@Data
@TableName("bus_item_attachment")
public class BusItemAttachment implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long itemId;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
}
