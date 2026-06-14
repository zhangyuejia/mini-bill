package com.minibill.common.service;

/**
 * 字典标签提供者接口
 * 在 common 模块定义契约，由各业务模块实现
 */
public interface DictLabelProvider {

    /**
     * 根据字典编码和键值获取中文标签
     */
    String getDictLabelByCodeAndValue(String dictCode, String value);
}
