package com.minibill.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.common.exception.BusinessException;
import com.minibill.common.util.RedisUtil;
import com.minibill.system.entity.SysDictData;
import com.minibill.system.entity.SysDictType;
import com.minibill.system.mapper.SysDictDataMapper;
import com.minibill.system.mapper.SysDictTypeMapper;
import com.minibill.system.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.minibill.common.constant.Constants.*;

/**
 * 字典管理服务实现
 */
@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictDataMapper dictDataMapper;
    private final RedisUtil redisUtil;

    // ===== 字典类型 =====

    @Override
    public Page<SysDictType> pageDictType(Integer pageNum, Integer pageSize, String keyword) {
        Page<SysDictType> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDelFlag, DEL_FLAG_NORMAL)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(SysDictType::getName, keyword)
                        .or()
                        .like(SysDictType::getCode, keyword))
                .orderByDesc(SysDictType::getCreateTime);
        return dictTypeMapper.selectPage(page, wrapper);
    }

    @Override
    public List<SysDictType> listDictType() {
        return dictTypeMapper.selectList(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDelFlag, DEL_FLAG_NORMAL)
                .eq(SysDictType::getStatus, STATUS_ENABLE));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDictType(SysDictType dictType) {
        Long count = dictTypeMapper.selectCount(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getCode, dictType.getCode())
                .eq(SysDictType::getDelFlag, DEL_FLAG_NORMAL));
        if (count > 0) {
            throw new BusinessException("字典编码已存在");
        }
        dictTypeMapper.insert(dictType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDictType(SysDictType dictType) {
        dictTypeMapper.updateById(dictType);
        redisUtil.delete(CACHE_KEY_DICT + dictType.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictType(Long id) {
        SysDictType type = dictTypeMapper.selectById(id);
        if (type != null) {
            dictDataMapper.delete(new LambdaQueryWrapper<SysDictData>()
                    .eq(SysDictData::getDictTypeId, id));
            type.setDelFlag(DEL_FLAG_DELETED);
            dictTypeMapper.updateById(type);
            redisUtil.delete(CACHE_KEY_DICT + type.getCode());
        }
    }

    // ===== 字典数据 =====

    @Override
    public Page<SysDictData> pageDictData(Integer pageNum, Integer pageSize, Long dictTypeId) {
        Page<SysDictData> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictTypeId, dictTypeId)
                .eq(SysDictData::getDelFlag, DEL_FLAG_NORMAL)
                .orderByAsc(SysDictData::getSort);
        return dictDataMapper.selectPage(page, wrapper);
    }

    @Override
    public List<SysDictData> getDictDataByCode(String dictCode) {
        @SuppressWarnings("unchecked")
        List<SysDictData> cached = redisUtil.get(CACHE_KEY_DICT + dictCode);
        if (cached != null) {
            return cached;
        }

        SysDictType type = dictTypeMapper.selectOne(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getCode, dictCode)
                .eq(SysDictType::getDelFlag, DEL_FLAG_NORMAL));
        if (type == null) {
            return List.of();
        }

        List<SysDictData> list = dictDataMapper.selectList(new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictTypeId, type.getId())
                .eq(SysDictData::getDelFlag, DEL_FLAG_NORMAL)
                .eq(SysDictData::getStatus, STATUS_ENABLE)
                .orderByAsc(SysDictData::getSort));

        redisUtil.set(CACHE_KEY_DICT + dictCode, list);
        return list;
    }

    @Override
    public String getDictLabelByCodeAndValue(String dictCode, String value) {
        List<SysDictData> list = getDictDataByCode(dictCode);
        return list.stream()
                .filter(d -> d.getValue().equals(value))
                .map(SysDictData::getLabel)
                .findFirst()
                .orElse(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDictData(SysDictData dictData) {
        dictDataMapper.insert(dictData);
        SysDictType type = dictTypeMapper.selectById(dictData.getDictTypeId());
        if (type != null) {
            redisUtil.delete(CACHE_KEY_DICT + type.getCode());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDictData(SysDictData dictData) {
        dictDataMapper.updateById(dictData);
        SysDictType type = dictTypeMapper.selectById(dictData.getDictTypeId());
        if (type != null) {
            redisUtil.delete(CACHE_KEY_DICT + type.getCode());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictData(Long id) {
        SysDictData data = dictDataMapper.selectById(id);
        if (data != null) {
            dictDataMapper.deleteById(id);
            SysDictType type = dictTypeMapper.selectById(data.getDictTypeId());
            if (type != null) {
                redisUtil.delete(CACHE_KEY_DICT + type.getCode());
            }
        }
    }
}
