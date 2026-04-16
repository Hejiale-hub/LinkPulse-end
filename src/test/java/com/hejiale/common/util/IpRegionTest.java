package com.hejiale.common.util;

import org.junit.jupiter.api.Test;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.InputStream;
import java.util.Scanner;

class IpRegionTest {

    @Test
    void testIpRegionParse() throws Exception {
        ClassPathResource resource = new ClassPathResource("ip2region_v4.xdb");
        InputStream inputStream = resource.getInputStream();
        byte[] cBuff = FileCopyUtils.copyToByteArray(inputStream);
        Searcher searcher = Searcher.newWithBuffer(cBuff);

        Scanner scanner = new Scanner(System.in);
        System.out.println("========== IP 地址解析测试 ==========");
        System.out.println("输入 IP 地址进行解析，输入 q 退出");
        System.out.println("====================================");

        while (true) {
            System.out.print("\n请输入 IP 地址: ");
            String ip = scanner.nextLine().trim();
            if ("q".equalsIgnoreCase(ip)) {
                System.out.println("退出测试");
                break;
            }
            if (ip.isEmpty()) {
                continue;
            }

            try {
                String regionStr = searcher.search(ip);
                System.out.println("原始结果: " + regionStr);

                if (regionStr != null && regionStr.contains("|")) {
                    String[] regions = regionStr.split("\\|");
                    System.out.println("国家:   " + regions[0]);
                    System.out.println("区域:   " + regions[1]);
                    System.out.println("省份:   " + regions[2]);
                    System.out.println("城市:   " + regions[3]);
                    System.out.println("ISP:    " + regions[4]);
                }
            } catch (Exception e) {
                System.out.println("解析失败: " + e.getMessage());
            }
        }

        scanner.close();
    }
}
