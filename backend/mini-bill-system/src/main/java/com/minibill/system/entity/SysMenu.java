package com.minibill.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minibill.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 系统菜单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity {

    private String name;       // 菜单名称
    private String path;       // 路由路径
    private String component;  // 组件路径
    private String permission; // 权限标识
    private Integer type;      // 0-目录 1-菜单 2-按钮
    private Long parentId;    // 父菜单ID
    private Integer sort;     // 排序
    private String icon;      // 图标
    private Integer visible;  // 0-显示 1-隐藏

    // 非数据库字段
    private transient List<SysMenu> children;
}
