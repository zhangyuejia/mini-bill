package com.minibill.bus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.bus.entity.BusEducation;
import org.apache.ibatis.annotations.Param;

public interface BusEducationMapper extends BaseMapper<BusEducation> {

    Page<BusEducation> pageWithMember(Page<BusEducation> page,
                                      @Param("familyId") Long familyId,
                                      @Param("memberId") Long memberId,
                                      @Param("semesterDateStart") String semesterDateStart,
                                      @Param("semesterDateEnd") String semesterDateEnd);
}
