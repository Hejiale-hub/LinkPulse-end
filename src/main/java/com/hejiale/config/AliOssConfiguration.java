package com.hejiale.config;

import com.hejiale.common.Properties.AliOssProperties;
import com.hejiale.common.util.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AliOssConfiguration {

    //如果没有这个配置类，你需要在每个使用AliOssUtil的地方都手动读取aliOssProperties配置并创建实例，这会导致代码重复和维护困难。
    //  因此，我们创建这个配置类，服务启动后自动将AliOssUtil实例化并返回交给ioc容器管理，这样，我们只需要在需要使用AliOssUtil的地方注入AliOssUtil实例，就可以使用它了。减少重复代码和便于维护。
    @Bean
    public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties) {
        log.info("开始创建阿里云OSS文件上传对象: {}", aliOssProperties);
        return new AliOssUtil(
                aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getBucketName());
    }
}
