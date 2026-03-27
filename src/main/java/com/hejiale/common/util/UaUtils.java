package com.hejiale.common.util;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

public class UaUtils {

    // 初始化解析器，这可能需要一点时间，所以作为单例静态加载
    private static final UserAgentAnalyzer uaa = UserAgentAnalyzer
            .newBuilder()
            .hideMatcherLoadStats()
            .withCache(10000)
            .build();

    public static UserAgent parse(String userAgentString) {
        return uaa.parse(userAgentString);
    }
}