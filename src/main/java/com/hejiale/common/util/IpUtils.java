package com.hejiale.common.util;

import com.hejiale.common.domain.po.RequestInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.InputStream;

public class IpUtils {

    private static Searcher searcher;

    static {
        try {
            // 从 classpath 加载 xdb 文件到内存，提高查询性能
            ClassPathResource resource = new ClassPathResource("ip2region.xdb");
            InputStream inputStream = resource.getInputStream();
            byte[] cBuff = FileCopyUtils.copyToByteArray(inputStream);
            searcher = Searcher.newWithBuffer(cBuff);
        } catch (Exception e) {
            System.err.println("初始化 ip2region 失败: " + e.getMessage());
        }
    }


    /**
     * 获取客户端真实 IP
     */
    public static String getIpAddress(RequestInfo request) {
        String ip = request.getHeader().get("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader().get("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader().get("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader().get("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader().get("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader().get("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For 可能包含多个 IP（经过多个代理），取第一个真实客户端 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(0, ip.indexOf(",")).trim();
        }
        return ip;
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