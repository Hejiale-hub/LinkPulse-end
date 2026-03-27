package com.hejiale.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;


import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtils {
    /**
     * 生成 jwt
     * 使用 Hs256 算法，私匙使用固定秘钥
     *
     * @param secretKey jwt 秘钥
     * @param ttlMillis jwt 过期时间 (毫秒)
     * @param claims    设置的信息
     * @return
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        // 生成 JWT 的时间
        Date now = new Date();
        Date exp = new Date(now.getTime() + ttlMillis);

        // 创建 SecretKey 对象
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        // 设置 jwt 的 body
        return Jwts.builder()
                .claims(claims)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    /**
     * Token 解密
     *
     * @param secretKey jwt 秘钥 此秘钥一定要保留好在服务端，不能暴露出去，否则 sign 就可以被伪造，如果对接多个客户端建议改造成多个
     * @param token     加密后的 token
     * @return
     */
    public static Claims parseJWT(String secretKey, String token) {
        // 创建 SecretKey 对象
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        // 解析 jwt
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
