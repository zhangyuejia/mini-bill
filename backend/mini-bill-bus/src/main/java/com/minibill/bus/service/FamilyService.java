package com.minibill.bus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.bus.entity.BusFamily;
import com.minibill.bus.entity.BusFamilyMember;

import java.util.List;

/**
 * 家庭管理服务接口
 */
public interface FamilyService {

    List<BusFamily> getUserFamilies(Long userId);

    BusFamily createFamily(Long userId, String familyName);

    void inviteMember(Long familyId, Long userId, String email);

    void removeMember(Long familyId, Long userId, Long targetUserId);

    List<BusFamilyMember> getFamilyMembers(Long familyId);

    void setDefaultFamily(Long userId, Long familyId);

    Page<BusFamily> pageFamily(Integer pageNum, Integer pageSize, String keyword);

    BusFamily getFamilyById(Long id);

    void updateFamily(BusFamily family);
}
