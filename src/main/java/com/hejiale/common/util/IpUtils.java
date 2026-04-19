package com.hejiale.common.util;

import com.hejiale.common.domain.po.RequestInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.InputStream;
import java.util.Map;

public class IpUtils {

    private static Searcher searcher;

    static {
        try {
            // 从 classpath 加载 xdb 文件到内存，提高查询性能
            ClassPathResource resource = new ClassPathResource("ip2region_v4.xdb");
            InputStream inputStream = resource.getInputStream();
            byte[] cBuff = FileCopyUtils.copyToByteArray(inputStream);
            searcher = Searcher.newWithBuffer(cBuff);
        } catch (Exception e) {
            System.err.println("初始化 ip2region 失败: " + e.getMessage());
        }
    }


    /**
     * 获取客户端真实 IP（Servlet 请求，用于 Filter / 拦截器）
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ip = headerFirstNonBlank(request, "X-Forwarded-For");
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = headerFirstNonBlank(request, "X-Real-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = headerFirstNonBlank(request, "Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = headerFirstNonBlank(request, "WL-Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = headerFirstNonBlank(request, "HTTP_CLIENT_IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = headerFirstNonBlank(request, "HTTP_X_FORWARDED_FOR");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return normalizeForwardedChain(ip);
    }

    /**
     * 获取客户端真实 IP，用于重定向业务逻辑中（RequestInfo 请求对象）
     */
    public static String getIpAddress(RequestInfo request) {
        Map<String, String> headers = request.getHeader();
        String ip = getHeaderIgnoreCase(headers, "X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = getHeaderIgnoreCase(headers, "X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = getHeaderIgnoreCase(headers, "Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = getHeaderIgnoreCase(headers, "WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = getHeaderIgnoreCase(headers, "HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = getHeaderIgnoreCase(headers, "HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return normalizeForwardedChain(ip);
    }

    private static String headerFirstNonBlank(HttpServletRequest request, String name) {
        String v = request.getHeader(name);
        return v != null ? v.trim() : null;
    }

    private static String normalizeForwardedChain(String ip) {
        if (ip != null && ip.contains(",")) {
            return ip.substring(0, ip.indexOf(',')).trim();
        }
        return ip;
    }

    private static String getHeaderIgnoreCase(Map<String, String> headers, String name) {
        String value = headers.get(name);
        if (value != null) {
            return value;
        }
        return headers.get(name.toLowerCase());
    }

    /**
     * 根据 IP 获取城市信息
     * 返回格式通常为：国家|区域|省份|城市|ISP
     */
    public static String getRegion(String ip) {
        try {
            return searcher.search(ip);
        } catch (Exception e) {
            return "未知";
        }
    }
}