package com.minibill.system.controller;

import com.minibill.common.result.Result;
import com.minibill.system.service.AuthService;
import com.minibill.system.vo.LoginRequestVO;
import com.minibill.system.vo.LoginVO;
import com.minibill.system.vo.RegisterVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequestVO request) {
        return Result.success(authService.login(request));
    }

    @Operation(summary = "邮箱注册")
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterVO request) {
        return Result.success(authService.register(request));
    }

    @Operation(summary = "发送邮箱验证码")
    @PostMapping("/sendEmailCode")
    public Result<String> sendEmailCode(@RequestParam String email) {
        authService.sendEmailCode(email);
        return Result.success("验证码已发送");
    }
}
