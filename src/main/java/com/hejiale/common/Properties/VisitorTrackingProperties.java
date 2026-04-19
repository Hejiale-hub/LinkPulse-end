package com.hejiale.common.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 访客统计：路径与同步频率（适配 Nginx 仅转发 /api 等到后端的部署）
 */
@Component
@ConfigurationProperties(prefix = "link.visitor-tracking")
public class VisitorTrackingProperties {

    /**
     * Servlet 上 API 的统一前缀，如 Nginx 将 /api 转到后端则填 /api。用于拼接文档类排除路径。
     */
    private String servletPathPrefix = "";

    /**
     * 为 true 时仅统计路径以 servletPathPrefix 开头的请求（适合静态与页面完全不走 Java、仅 /api 进后端的部署）。
     */
    private boolean recordOnlyUnderPrefix = false;

    /**
     * 将「当天」Redis 快照同步到 MySQL 的间隔（毫秒），不删 Redis。
     */
    private long syncFixedRateMs = 300_000L;

    /** 为 false 时不执行定时快照同步（仍保留每日 0:05 归档昨日并删 Redis）。 */
    private boolean syncEnabled = true;

    public String getServletPathPrefix() {
        return servletPathPrefix;
    }

    public void setServletPathPrefix(String servletPathPrefix) {
        this.servletPathPrefix = servletPathPrefix == null ? "" : servletPathPrefix.trim();
    }

    public boolean isRecordOnlyUnderPrefix() {
        return recordOnlyUnderPrefix;
    }

    public void setRecordOnlyUnderPrefix(boolean recordOnlyUnderPrefix) {
        this.recordOnlyUnderPrefix = recordOnlyUnderPrefix;
    }

    public long getSyncFixedRateMs() {
        return syncFixedRateMs;
    }

    public void setSyncFixedRateMs(long syncFixedRateMs) {
        this.syncFixedRateMs = syncFixedRateMs;
    }

    public boolean isSyncEnabled() {
        return syncEnabled;
    }

    public void setSyncEnabled(boolean syncEnabled) {
        this.syncEnabled = syncEnabled;
    }
}
