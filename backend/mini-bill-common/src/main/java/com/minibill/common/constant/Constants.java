package com.minibill.common.constant;

/**
 * 系统常量
 */
public interface Constants {

    String TOKEN_HEADER = "Authorization";
    String TOKEN_PREFIX = "Bearer ";
    String TOKEN_SECRET = "mini-bill-secret-key-2024-zhang-family-finance";
    long TOKEN_EXPIRATION = 86400000; // 24小时

    String REDIS_KEY_PREFIX = "mini-bill:";
    String REDIS_CAPTCHA_KEY = REDIS_KEY_PREFIX + "captcha:";

    String SYS_ADMIN_ROLE = "ADMIN";
    String SYS_DEFAULT_ROLE = "USER";

    Integer DEL_FLAG_NORMAL = 0;
    Integer DEL_FLAG_DELETED = 1;

    Integer STATUS_ENABLE = 0;
    Integer STATUS_DISABLE = 1;

    String CACHE_KEY_USER = REDIS_KEY_PREFIX + "user:";
    String CACHE_KEY_MENU = REDIS_KEY_PREFIX + "menu:";
    String CACHE_KEY_DICT = REDIS_KEY_PREFIX + "dict:";

    /** 附件业务类型 */
    String BIZ_TYPE_BILL = "bill";
    String BIZ_TYPE_ITEM = "item";
    String BIZ_TYPE_ITEM_COST = "item_cost";
    String BIZ_TYPE_MAINTENANCE = "maintenance";
}
