package com.minibill.system.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 登录响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {

    private String token;
    private UserInfoVO userInfo;
    private List<String> roles;
    private List<String> permissions;
    private List<MenuVO> menus;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoVO {
        private Long id;
        private String username;
        private String nickname;
        private String email;
        private String avatar;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuVO {
        private Long id;
        private String name;
        private String path;
        private String component;
        private String permission;
        private Integer type;
        private Long parentId;
        private Integer sort;
        private String icon;
        private List<MenuVO> children;
    }
}
