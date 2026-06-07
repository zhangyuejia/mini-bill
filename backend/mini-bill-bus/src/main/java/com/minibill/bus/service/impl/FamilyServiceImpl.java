package com.minibill.bus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.api.feign.SystemFeignClient;
import com.minibill.common.exception.BusinessException;
import com.minibill.bus.entity.BusFamily;
import com.minibill.bus.entity.BusFamilyMember;
import com.minibill.bus.mapper.BusFamilyMapper;
import com.minibill.bus.mapper.BusFamilyMemberMapper;
import com.minibill.bus.service.FamilyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.minibill.common.constant.Constants.*;

/**
 * 家庭管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FamilyServiceImpl implements FamilyService {

    private final BusFamilyMapper familyMapper;
    private final BusFamilyMemberMapper memberMapper;
    private final SystemFeignClient systemFeignClient;

    @Override
    public List<BusFamily> getUserFamilies(Long userId) {
        List<Long> familyIds = memberMapper.selectList(
                        new LambdaQueryWrapper<BusFamilyMember>()
                                .eq(BusFamilyMember::getUserId, userId)
                                .eq(BusFamilyMember::getStatus, STATUS_ENABLE))
                .stream()
                .map(BusFamilyMember::getFamilyId)
                .toList();

        if (familyIds.isEmpty()) {
            return List.of();
        }

        List<BusFamily> families = familyMapper.selectList(new LambdaQueryWrapper<BusFamily>()
                .in(BusFamily::getId, familyIds)
                .eq(BusFamily::getStatus, STATUS_ENABLE)
                .eq(BusFamily::getDelFlag, DEL_FLAG_NORMAL));

        // 填充户主名称
        for (BusFamily f : families) {
            try {
                var user = systemFeignClient.getUserById(f.getOwnerId());
                if (user != null) {
                    f.setOwnerName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                }
            } catch (Exception e) {
                log.warn("获取户主信息失败, ownerId={}", f.getOwnerId());
            }
        }
        return families;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BusFamily createFamily(Long userId, String familyName) {
        BusFamily family = new BusFamily();
        family.setName(familyName);
        family.setOwnerId(userId);
        family.setDefaultFlag(0);
        family.setStatus(STATUS_ENABLE);
        familyMapper.insert(family);

        BusFamilyMember member = new BusFamilyMember();
        member.setFamilyId(family.getId());
        member.setUserId(userId);
        member.setRole("owner");
        member.setStatus(STATUS_ENABLE);
        member.setJoinTime(LocalDateTime.now());
        memberMapper.insert(member);

        long count = memberMapper.selectCount(new LambdaQueryWrapper<BusFamilyMember>()
                .eq(BusFamilyMember::getUserId, userId)
                .eq(BusFamilyMember::getStatus, STATUS_ENABLE));
        if (count == 1) {
            family.setDefaultFlag(1);
            familyMapper.updateById(family);
        }

        return family;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inviteMember(Long familyId, Long userId, String email) {
        BusFamily family = familyMapper.selectById(familyId);
        if (family == null || !family.getOwnerId().equals(userId)) {
            throw new BusinessException("只有一家之主才能邀请成员");
        }

        // 通过邮箱查找用户
        var userDto = systemFeignClient.getUserByEmail(email);
        if (userDto == null) {
            throw new BusinessException("该邮箱未注册");
        }

        Long targetUserId = userDto.getId();
        boolean isMember = memberMapper.isFamilyMember(familyId, targetUserId);
        if (isMember) {
            throw new BusinessException("该用户已经是家庭成员");
        }

        BusFamilyMember member = new BusFamilyMember();
        member.setFamilyId(familyId);
        member.setUserId(targetUserId);
        member.setRole("member");
        member.setStatus(STATUS_ENABLE);
        member.setJoinTime(LocalDateTime.now());
        memberMapper.insert(member);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMember(Long familyId, Long userId, Long targetUserId) {
        BusFamily family = familyMapper.selectById(familyId);
        if (family == null || !family.getOwnerId().equals(userId)) {
            throw new BusinessException("只有一家之主才能移出成员");
        }
        if (targetUserId.equals(family.getOwnerId())) {
            throw new BusinessException("不能移出自己");
        }

        memberMapper.delete(new LambdaQueryWrapper<BusFamilyMember>()
                .eq(BusFamilyMember::getFamilyId, familyId)
                .eq(BusFamilyMember::getUserId, targetUserId));
    }

    @Override
    public List<BusFamilyMember> getFamilyMembers(Long familyId) {
        List<BusFamilyMember> members = memberMapper.selectList(new LambdaQueryWrapper<BusFamilyMember>()
                .eq(BusFamilyMember::getFamilyId, familyId)
                .eq(BusFamilyMember::getStatus, STATUS_ENABLE));
        // 填充用户名
        for (BusFamilyMember m : members) {
            try {
                var user = systemFeignClient.getUserById(m.getUserId());
                if (user != null) {
                    m.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                }
            } catch (Exception e) {
                log.warn("获取用户信息失败, userId={}", m.getUserId());
            }
        }
        return members;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultFamily(Long userId, Long familyId) {
        List<Long> familyIds = memberMapper.selectList(
                        new LambdaQueryWrapper<BusFamilyMember>()
                                .eq(BusFamilyMember::getUserId, userId))
                .stream()
                .map(BusFamilyMember::getFamilyId)
                .toList();

        if (!familyIds.isEmpty()) {
            familyMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<BusFamily>()
                            .in(BusFamily::getId, familyIds)
                            .set(BusFamily::getDefaultFlag, 0));
        }

        BusFamily family = familyMapper.selectById(familyId);
        if (family != null) {
            family.setDefaultFlag(1);
            familyMapper.updateById(family);
        }
    }

    @Override
    public Page<BusFamily> pageFamily(Integer pageNum, Integer pageSize, String keyword) {
        Page<BusFamily> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BusFamily> wrapper = new LambdaQueryWrapper<BusFamily>()
                .eq(BusFamily::getDelFlag, DEL_FLAG_NORMAL)
                .like(keyword != null, BusFamily::getName, keyword)
                .orderByDesc(BusFamily::getCreateTime);
        return familyMapper.selectPage(page, wrapper);
    }

    @Override
    public BusFamily getFamilyById(Long id) {
        BusFamily family = familyMapper.selectById(id);
        if (family == null) {
            throw new BusinessException("家庭不存在");
        }
        return family;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFamily(BusFamily family) {
        BusFamily exist = familyMapper.selectById(family.getId());
        if (exist == null) {
            throw new BusinessException("家庭不存在");
        }
        exist.setName(family.getName());
        familyMapper.updateById(exist);
    }
}
