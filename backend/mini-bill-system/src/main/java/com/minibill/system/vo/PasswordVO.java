package com.minibill.system.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重置密码/修改密码请求VO
 */
@Data
public class PasswordVO {

    private Long userId;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度为6-32个字符")
    private String newPassword;

    private String oldPassword; // 修改密码时必填
}
