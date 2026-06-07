package com.minibill.bus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minibill.bus.entity.BusFamilyMember;
import org.apache.ibatis.annotations.Param;

public interface BusFamilyMemberMapper extends BaseMapper<BusFamilyMember> {

    boolean isFamilyMember(@Param("familyId") Long familyId, @Param("userId") Long userId);
}
