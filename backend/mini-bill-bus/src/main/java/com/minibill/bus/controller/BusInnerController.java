package com.minibill.bus.controller;

import com.minibill.bus.mapper.BusFamilyMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 业务服务内部Feign接口
 */
@RestController
@RequestMapping("/api/inner")
@RequiredArgsConstructor
public class BusInnerController {

    private final BusFamilyMemberMapper memberMapper;

    @GetMapping("/family/{familyId}/member/{userId}")
    public Boolean isFamilyMember(@PathVariable Long familyId, @PathVariable Long userId) {
        return memberMapper.isFamilyMember(familyId, userId);
    }
}
