package com.minibill.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.system.entity.SysDictData;
import com.minibill.system.entity.SysDictType;

import java.util.List;

/**
 * 字典管理服务接口
 */
public interface DictService {

    // ===== 字典类型 =====

    Page<SysDictType> pageDictType(Integer pageNum, Integer pageSize, String keyword);

    List<SysDictType> listDictType();

    void addDictType(SysDictType dictType);

    void updateDictType(SysDictType dictType);

    void deleteDictType(Long id);

    // ===== 字典数据 =====

    Page<SysDictData> pageDictData(Integer pageNum, Integer pageSize, Long dictTypeId);

    List<SysDictData> getDictDataByCode(String dictCode);

    /**
     * 根据字典编码和键值获取中文标签（自动回填缓存）
     */
    String getDictLabelByCodeAndValue(String dictCode, String value);

    void addDictData(SysDictData dictData);

    void updateDictData(SysDictData dictData);

    void deleteDictData(Long id);
}
