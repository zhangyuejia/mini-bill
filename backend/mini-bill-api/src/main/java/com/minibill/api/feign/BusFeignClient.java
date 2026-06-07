package com.minibill.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 业务服务 Feign 客户端
 */
@FeignClient(name = "mini-bill-bus", path = "/bus")
public interface BusFeignClient {

    /**
     * 检查家庭成员是否属于某家庭
     */
    @GetMapping("/api/inner/family/{familyId}/member/{userId}")
    Boolean isFamilyMember(@PathVariable("familyId") Long familyId, @PathVariable("userId") Long userId);
}
