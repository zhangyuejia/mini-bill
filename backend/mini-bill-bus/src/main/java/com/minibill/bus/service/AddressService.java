package com.minibill.bus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.bus.entity.BusAddress;

import java.util.List;

/**
 * 住址管理服务接口
 */
public interface AddressService {

    List<BusAddress> getAddressesByFamily(Long familyId);

    Page<BusAddress> pageAddress(Integer pageNum, Integer pageSize, Long familyId);

    BusAddress getAddressById(Long id);

    void addAddress(BusAddress address);

    void updateAddress(BusAddress address);

    void deleteAddress(Long id);
}
