package com.hejiale.common.util;


import com.hejiale.common.constants.UrlConstants;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LinkUtils {
    private static final String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int SCALE = 62;


    /**
     * 将数字(ID)转换为62进制字符串
     */
    public static String encode(long num) {
        StringBuilder sb = new StringBuilder();

        // 62进制转换
        while (num > 0) {
            int i = (int) (num % SCALE);
            sb.append(CHARS.charAt(i));
            num /= SCALE;
        }
        // 反转字符串
        sb.reverse();

        return sb.toString();
    }

    /**
     * 62进制字符串转回数字 (可选，用于反向校验)
     */
    public static long decode(String str) {
        long num = 0;
        int len = str.length();
        for (int i = 0; i < len; i++) {
            num += CHARS.indexOf(str.charAt(i)) * Math.pow(SCALE, len - i - 1);
        }
        return num;
    }
}
