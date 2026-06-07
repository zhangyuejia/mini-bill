package com.minibill.common.exception;

/**
 * 未授权异常
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("未登录或token已过期");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
