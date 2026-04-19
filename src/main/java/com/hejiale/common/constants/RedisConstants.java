package com.hejiale.common.constants;

public interface RedisConstants {
    String LINK_CODE_PREFIX_CACHE_KEY = "link:code:"; // 存储短链接code的前缀
    String LINK_ID_PREFIX_CACHE_KEY = "link:id:";     // 存储短链接id的前缀
    String EMPTY_CACHE = "EMPTY_CACHE"; // 空值缓存，防止缓存穿透
    /** 访客按日统计 Hash：visitor:day:{yyyyMMdd} → field=ip, value=次数 */
    String VISITOR_DAY_KEY_PREFIX = "visitor:day:";
}
