package com.minibill.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.minibill.common.constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "delFlag", () -> Constants.DEL_FLAG_NORMAL, Integer.class);
        this.strictInsertFill(metaObject, "createBy", () -> getCurrentUserId(), Long.class);
        this.strictInsertFill(metaObject, "updateBy", () -> getCurrentUserId(), Long.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
        this.strictUpdateFill(metaObject, "updateBy", () -> getCurrentUserId(), Long.class);
    }

    /**
     * 获取当前用户ID（从SecurityContextHolder获取，需各模块实现）
     * 默认返回0，子模块可覆盖
     */
    private Long getCurrentUserId() {
        // 通过ThreadLocal等方式获取，这里由各模块在过滤器/拦截器中注入
        Long userId = SecurityContextHolder.getUserId();
        return userId != null ? userId : 0L;
    }
}
