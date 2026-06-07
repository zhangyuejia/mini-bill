package com.minibill.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.minibill.system.entity.SysUser;

import java.util.List;

/**
 * 用户管理服务接口
 */
public interface UserService {

    Page<SysUser> pageUser(Integer pageNum, Integer pageSize, String keyword);

    SysUser getUserById(Long id);

    List<Long> getUserRoleIds(Long userId);

    void assignRoles(Long userId, List<Long> roleIds);

    void setUserStatus(Long userId, Integer status);

    void resetPassword(Long userId, String newPassword);

    void changePassword(Long userId, String oldPassword, String newPassword);
}
