package com.hejiale.config;

import com.hejiale.common.Properties.VisitorTrackingProperties;
import com.hejiale.filter.VisitorTrackingFilter;
import com.hejiale.service.IVisitorStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
@RequiredArgsConstructor
public class VisitorTrackingConfiguration {

    private final IVisitorStatService visitorStatService;
    private final VisitorTrackingProperties visitorTrackingProperties;

    @Bean
    public FilterRegistrationBean<VisitorTrackingFilter> visitorTrackingFilter() {
        FilterRegistrationBean<VisitorTrackingFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new VisitorTrackingFilter(visitorStatService, visitorTrackingProperties));
        reg.addUrlPatterns("/*");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 40);
        reg.setName("visitorTrackingFilter");
        return reg;
    }
}