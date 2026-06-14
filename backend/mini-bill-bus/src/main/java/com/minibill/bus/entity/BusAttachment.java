package com.minibill.bus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用附件
 */
@Data
@TableName("bus_attachment")
public class BusAttachment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String bizType;
    private Long bizId;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
}
