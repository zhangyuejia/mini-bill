package com.minibill.bus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.common.exception.BusinessException;
import com.minibill.bus.entity.BusAddress;
import com.minibill.bus.mapper.BusAddressMapper;
import com.minibill.bus.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.minibill.common.constant.Constants.*;

/**
 * 住址管理服务实现
 */
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final BusAddressMapper addressMapper;

    @Override
    public List<BusAddress> getAddressesByFamily(Long familyId) {
        return addressMapper.selectList(new LambdaQueryWrapper<BusAddress>()
                .eq(BusAddress::getFamilyId, familyId)
                .eq(BusAddress::getDelFlag, DEL_FLAG_NORMAL)
                .eq(BusAddress::getStatus, STATUS_ENABLE));
    }

    @Override
    public Page<BusAddress> pageAddress(Integer pageNum, Integer pageSize, Long familyId) {
        Page<BusAddress> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BusAddress> wrapper = new LambdaQueryWrapper<BusAddress>()
                .eq(BusAddress::getFamilyId, familyId)
                .eq(BusAddress::getDelFlag, DEL_FLAG_NORMAL)
                .orderByDesc(BusAddress::getCreateTime);
        return addressMapper.selectPage(page, wrapper);
    }

    @Override
    public BusAddress getAddressById(Long id) {
        BusAddress address = addressMapper.selectById(id);
        if (address == null) {
            throw new BusinessException("住址不存在");
        }
        return address;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAddress(BusAddress address) {
        address.setStatus(STATUS_ENABLE);
        addressMapper.insert(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(BusAddress address) {
        addressMapper.updateById(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAddress(Long id) {
        BusAddress address = addressMapper.selectById(id);
        if (address != null) {
            address.setDelFlag(DEL_FLAG_DELETED);
            addressMapper.updateById(address);
        }
    }
}
