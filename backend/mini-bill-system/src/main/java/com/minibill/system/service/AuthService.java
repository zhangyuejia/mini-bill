package com.minibill.system.service;

import com.minibill.system.vo.LoginRequestVO;
import com.minibill.system.vo.LoginVO;
import com.minibill.system.vo.RegisterVO;

/**
 * 认证服务接口
 */
public interface AuthService {

    LoginVO login(LoginRequestVO request);

    LoginVO register(RegisterVO request);

    void sendEmailCode(String email);
}
