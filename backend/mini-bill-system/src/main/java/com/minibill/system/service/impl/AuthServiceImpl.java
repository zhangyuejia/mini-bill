package com.minibill.system.service.impl;

import com.minibill.common.exception.BusinessException;
import com.minibill.common.util.JwtUtil;
import com.minibill.common.util.RedisUtil;
import com.minibill.system.entity.*;
import com.minibill.system.mapper.*;
import com.minibill.system.service.AuthService;
import com.minibill.system.vo.LoginVO;
import com.minibill.system.vo.LoginRequestVO;
import com.minibill.system.vo.RegisterVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.minibill.common.constant.Constants.*;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final RedisUtil redisUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public LoginVO login(LoginRequestVO request) {
        // 支持用户名或邮箱登录
        SysUser user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getDelFlag, DEL_FLAG_NORMAL)
                        .and(w -> w.eq(SysUser::getUsername, request.getUsername())
                                .or()
                                .eq(SysUser::getEmail, request.getUsername())));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (STATUS_DISABLE.equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        return buildLoginVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterVO request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 校验邮箱验证码
        String cacheCode = redisUtil.get(REDIS_CAPTCHA_KEY + request.getEmail());
        if (cacheCode == null || !cacheCode.equals(request.getEmailCode())) {
            throw new BusinessException("邮箱验证码错误或已过期");
        }

        // 检查用户名是否已存在
        Long count = userMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, request.getUsername())
                        .eq(SysUser::getDelFlag, DEL_FLAG_NORMAL));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已注册
        count = userMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getEmail, request.getEmail())
                        .eq(SysUser::getDelFlag, DEL_FLAG_NORMAL));
        if (count > 0) {
            throw new BusinessException("该邮箱已被注册");
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNickname(request.getUsername());
        user.setStatus(STATUS_ENABLE);
        userMapper.insert(user);

        // 赋予默认角色
        SysRole defaultRole = roleMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getCode, SYS_DEFAULT_ROLE)
                        .eq(SysRole::getDelFlag, DEL_FLAG_NORMAL));
        if (defaultRole != null) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(defaultRole.getId());
            userRoleMapper.insert(userRole);
        }

        // 删除验证码缓存
        redisUtil.delete(REDIS_CAPTCHA_KEY + request.getEmail());

        return buildLoginVO(user);
    }

    @Override
    public void sendEmailCode(String email) {
        // TODO: 实际调用邮件服务发送验证码
        // 这里简化处理，直接生成验证码存入Redis
        String code = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
        redisUtil.set(REDIS_CAPTCHA_KEY + email, code, 5, TimeUnit.MINUTES);
        log.info("邮箱验证码 [{}] -> {}", email, code);
    }

    /**
     * 构建登录响应
     */
    private LoginVO buildLoginVO(SysUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        String token = JwtUtil.generateToken(user.getId(), user.getUsername(), claims);

        List<String> roles = userMapper.getRoleCodesByUserId(user.getId());
        List<String> permissions = userMapper.getPermissionsByUserId(user.getId());
        List<SysMenu> menus = menuMapper.getMenusByUserId(user.getId());

        LoginVO.UserInfoVO userInfo = LoginVO.UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .build();

        List<LoginVO.MenuVO> menuTree = buildMenuTree(menus);

        return LoginVO.builder()
                .token(token)
                .userInfo(userInfo)
                .roles(roles)
                .permissions(permissions)
                .menus(menuTree)
                .build();
    }

    /**
     * 构建菜单树
     */
    private List<LoginVO.MenuVO> buildMenuTree(List<SysMenu> menus) {
        List<LoginVO.MenuVO> voList = menus.stream().map(m -> LoginVO.MenuVO.builder()
                .id(m.getId())
                .name(m.getName())
                .path(m.getPath())
                .component(m.getComponent())
                .permission(m.getPermission())
                .type(m.getType())
                .parentId(m.getParentId())
                .sort(m.getSort())
                .icon(m.getIcon())
                .children(List.of())
                .build()).toList();

        return voList.stream()
                .filter(m -> m.getParentId() == null || m.getParentId() == 0)
                .map(m -> {
                    m.setChildren(getChildren(m.getId(), voList));
                    return m;
                })
                .toList();
    }

    private List<LoginVO.MenuVO> getChildren(Long parentId, List<LoginVO.MenuVO> allMenus) {
        return allMenus.stream()
                .filter(m -> parentId.equals(m.getParentId()))
                .map(m -> {
                    m.setChildren(getChildren(m.getId(), allMenus));
                    return m;
                })
                .toList();
    }
}
