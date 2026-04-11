package com.hejiale.common.util;

import com.hejiale.common.domain.vo.SessionUtilVO;

import java.util.concurrent.ThreadLocalRandom;

public class SessionUtils {
    public static SessionUtilVO generateChatId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        long timestamp = System.currentTimeMillis();
        int random = ThreadLocalRandom.current().nextInt(1000, 10000); // 4位随机数

        String chatId = String.format("%s_%d_%04x", userId, timestamp, random);
        String sessionTitle = "会话_" + chatId; // 可以根据需要生成会话标题
        SessionUtilVO sessionUtilVO = new SessionUtilVO();
        sessionUtilVO.setChatId(chatId);
        sessionUtilVO.setSessionTitle(sessionTitle);
        return sessionUtilVO;
    }
}
