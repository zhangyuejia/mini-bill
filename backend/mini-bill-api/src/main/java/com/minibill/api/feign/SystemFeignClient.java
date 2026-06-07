package com.minibill.api.feign;

import com.minibill.api.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 系统服务 Feign 客户端
 */
@FeignClient(name = "mini-bill-system", path = "/api/inner")
public interface SystemFeignClient {

    /**
     * 根据用户ID获取用户信息
     */
    @GetMapping("/user/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);

    /**
     * 根据用户名获取用户信息
     */
    @GetMapping("/user/byUsername")
    UserDTO getUserByUsername(@RequestParam("username") String username);

    /**
     * 根据用户ID获取角色编码列表
     */
    @GetMapping("/role/codes/{userId}")
    List<String> getRoleCodesByUserId(@PathVariable("userId") Long userId);

    /**
     * 根据用户ID获取权限标识列表
     */
    @GetMapping("/permission/{userId}")
    List<String> getPermissionsByUserId(@PathVariable("userId") Long userId);

    /**
     * 验证用户是否拥有某角色
     */
    @GetMapping("/user/hasRole")
    Boolean hasRole(@RequestParam("userId") Long userId, @RequestParam("roleCode") String roleCode);

    /**
     * 根据邮箱获取用户
     */
    @GetMapping("/user/byEmail")
    UserDTO getUserByEmail(@RequestParam("email") String email);
}
