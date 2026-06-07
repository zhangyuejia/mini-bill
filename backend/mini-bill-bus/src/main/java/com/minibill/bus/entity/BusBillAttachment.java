package com.minibill.bus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 账单附件
 */
@Data
@TableName("bus_bill_attachment")
public class BusBillAttachment implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long billId;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
}
