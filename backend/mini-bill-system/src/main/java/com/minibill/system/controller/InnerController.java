package com.minibill.system.controller;

import com.minibill.api.dto.UserDTO;
import com.minibill.system.entity.SysUser;
import com.minibill.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 内部Feign调用接口（不暴露到网关外部）
 */
@RestController
@RequestMapping("/api/inner")
@RequiredArgsConstructor
public class InnerController {

    private final SysUserMapper userMapper;

    @GetMapping("/user/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        SysUser user = userMapper.selectById(id);
        return toUserDTO(user);
    }

    @GetMapping("/user/byUsername")
    public UserDTO getUserByUsername(@RequestParam String username) {
        SysUser user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .eq(SysUser::getDelFlag, 0));
        return toUserDTO(user);
    }

    @GetMapping("/user/byEmail")
    public UserDTO getUserByEmail(@RequestParam String email) {
        SysUser user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getEmail, email)
                        .eq(SysUser::getDelFlag, 0));
        return toUserDTO(user);
    }

    @GetMapping("/role/codes/{userId}")
    public List<String> getRoleCodesByUserId(@PathVariable Long userId) {
        return userMapper.getRoleCodesByUserId(userId);
    }

    @GetMapping("/permission/{userId}")
    public List<String> getPermissionsByUserId(@PathVariable Long userId) {
        return userMapper.getPermissionsByUserId(userId);
    }

    @GetMapping("/user/hasRole")
    public Boolean hasRole(@RequestParam Long userId, @RequestParam String roleCode) {
        return userMapper.hasRole(userId, roleCode);
    }

    private UserDTO toUserDTO(SysUser user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setStatus(user.getStatus());
        dto.setCreateTime(user.getCreateTime());
        return dto;
    }
}
