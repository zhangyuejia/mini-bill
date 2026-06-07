package com.minibill.bus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 家庭成员
 */
@Data
@TableName("bus_family_member")
public class BusFamilyMember implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long familyId;
    private Long userId;
    private String role;      // owner-户主 member-成员
    private Integer status;   // 0-正常 1-已退出
    private LocalDateTime joinTime;

    // 非数据库字段（跨服务填充）
    private transient String userName;
}
