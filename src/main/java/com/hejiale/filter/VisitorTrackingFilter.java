package com.hejiale.filter;

import com.hejiale.common.Properties.VisitorTrackingProperties;
import com.hejiale.common.util.IpUtils;
import com.hejiale.service.IVisitorStatService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 全站请求访客计数；排除 Spring 文档与错误页等（静态若由 Nginx 直出则不会进入本 Filter）。
 */
public class VisitorTrackingFilter extends OncePerRequestFilter {

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    /** 仍可能由同一 Spring 进程提供的非业务路径（与是否带 /api 前缀无关的保留一份，便于本机直连调试） */
    private static final String[] RELATIVE_EXCLUDES = {
            "/error",
            "/actuator/**",
            "/doc.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/v2/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    private final IVisitorStatService visitorStatService;
    private final List<String> skipPatterns;
    private final String servletPathPrefix;
    private final boolean recordOnlyUnderPrefix;

    public VisitorTrackingFilter(IVisitorStatService visitorStatService, VisitorTrackingProperties properties) {
        this.visitorStatService = visitorStatService;
        this.servletPathPrefix = normalizePrefix(properties.getServletPathPrefix());
        this.recordOnlyUnderPrefix = properties.isRecordOnlyUnderPrefix();
        this.skipPatterns = buildSkipPatterns(this.servletPathPrefix);
    }

    private static List<String> buildSkipPatterns(String prefix) {
        List<String> patterns = new ArrayList<>();
        Collections.addAll(patterns, RELATIVE_EXCLUDES);
        if (StringUtils.hasText(prefix)) {
            for (String rel : RELATIVE_EXCLUDES) {
                patterns.add(prefix + rel);
            }
        }
        return Collections.unmodifiableList(patterns);
    }

    private static String normalizePrefix(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "";
        }
        String p = raw.trim();
        if (!p.startsWith("/")) {
            p = "/" + p;
        }
        while (p.endsWith("/") && p.length() > 1) {
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String path = normalizedServletPath(request);

        if (recordOnlyUnderPrefix && StringUtils.hasText(servletPathPrefix)) {
            if (!path.startsWith(servletPathPrefix) && !path.equals(servletPathPrefix)) {
                return true;
            }
        }

        for (String pattern : skipPatterns) {
            if (MATCHER.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private static String normalizedServletPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isEmpty() && path.startsWith(ctx)) {
            path = path.substring(ctx.length());
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            visitorStatService.recordVisit(IpUtils.getIpAddress(request));
        } catch (Exception ex) {
            logger.warn("visitor record skipped: " + ex.getMessage());
        }
        filterChain.doFilter(request, response);
    }
}
