package com.minibill.system.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求VO
 */
@Data
public class LoginRequestVO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
