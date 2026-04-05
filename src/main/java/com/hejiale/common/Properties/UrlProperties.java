package com.hejiale.common.Properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "link.redirect") // 修正前缀为规范形式
public class UrlProperties {
    private String urlPrefix;
}
