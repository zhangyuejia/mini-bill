package com.minibill.common.annotation;

import java.lang.annotation.*;

/**
 * 字典文本注解
 * 标记在字段上，自动将字典值转为中文文本，添加 {fieldName}Text 字段到返回体
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DictText {

    /**
     * 字典编码，对应 sys_dict_type.code
     */
    String dictCode();
}
