package com.hejiale.common.Properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "link.cors")
@Data
public class CorsProperties {

    String allowedOrigins; // 允许的源

}
