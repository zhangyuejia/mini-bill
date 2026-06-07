package com.minibill.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.minibill.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典数据
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dict_data")
public class SysDictData extends BaseEntity {

    private Long dictTypeId;
    private String label;
    private String value;
    private Integer sort;
    private Integer status; // 0-正常 1-禁用
    private String remark;
}
