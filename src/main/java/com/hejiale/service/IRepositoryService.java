package com.hejiale.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hejiale.domain.po.Repository;
import com.hejiale.domain.vo.SessionVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IRepositoryService extends IService<Repository> {
    /**
     * 保存会话历史
     * @param type 会话类型 （chat、service、pdf）
     * @param chatId 会话id
     */
    void save(String type, String chatId, String sessionTitle);

    /**
     * 获取会话ID列表      * @param type 会话类型 （chat、service、pdf）
     * @return 会话ID列表
     */
    List<SessionVO> getChatIds();

    /**
     * 删除会话历史
     * @param chatId 会话id
     */
    void deleteByChatId(String chatId);

    void updateSessionTitle(String chatId, String sessionTitle);

    SessionVO createChat(String type);

    void savePdfFile(String chatId, MultipartFile file) throws IOException;

    String getPdfAddress(String chatId);

    String getPdfName(String chatId);
}
