package com.hejiale.config;

import com.hejiale.common.Properties.CorsProperties;
import com.hejiale.interceptor.JwtTokenInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class WebMvcConfig extends WebMvcConfigurationSupport {

    private final JwtTokenInterceptor jwtTokenInterceptor;
    private final CorsProperties corsProperties;

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        //添加管理端拦截器
        registry.addInterceptor(jwtTokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/register",
                        "/user/login",
                        "/{linkCode}"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("开始配置跨域访问...");
        registry.addMapping("/**") // 拦截所有路径的请求
                // 允许跨域访问的源，生产环境应设置为前端应用的实际域名，如 "https://www.yourdomain.com"
                .allowedOrigins(corsProperties.getAllowedOrigins())
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}