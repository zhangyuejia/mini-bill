package com.minibill.system.config;

import com.minibill.common.config.SecurityContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 用户上下文过滤器
 * 从网关转发的请求头中提取用户信息，设置到SecurityContextHolder
 */
@Slf4j
@Component
public class UserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String userIdStr = request.getHeader("X-User-Id");
            if (StringUtils.hasText(userIdStr)) {
                SecurityContextHolder.setUserId(Long.valueOf(userIdStr));
            }

            String userName = request.getHeader("X-User-Name");
            if (StringUtils.hasText(userName)) {
                SecurityContextHolder.set("username", userName);
            }
            filterChain.doFilter(request, response);
        } finally {
            SecurityContextHolder.remove();
        }
    }
}
