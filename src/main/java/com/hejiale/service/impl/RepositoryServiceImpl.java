package com.hejiale.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hejiale.common.context.UserContext;
import com.hejiale.common.domain.vo.SessionUtilVO;
import com.hejiale.common.util.SessionUtils;
import com.hejiale.domain.po.Repository;
import com.hejiale.domain.vo.SessionVO;
import com.hejiale.mapper.RepositoryMapper;
import com.hejiale.service.IRepositoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RepositoryServiceImpl extends ServiceImpl<RepositoryMapper, Repository> implements IRepositoryService {
    /*
     *保存会话历史id
     */
    @Override
    public void save(String type, String chatId, String sessionTitle) {
        // 封装到实体类，并保存到数据库
        Repository repository = new Repository();
        repository.setType(type);
        repository.setChatId(chatId);
        repository.setUserId(UserContext.getUserId());
        repository.setSessionTitle(sessionTitle);
        save(repository);
    }

    ;

    @Override
    public List<SessionVO> getChatIds() {
        return lambdaQuery()
                .eq(Repository::getUserId, UserContext.getUserId())
                .list()
                .stream()
                .map(repository -> {
                    SessionVO sessionVO = new SessionVO();
                    sessionVO.setChatId(repository.getChatId());
                    sessionVO.setType(repository.getType());
                    sessionVO.setSessionTitle(repository.getSessionTitle());
                    return sessionVO;
                })
                .toList();
    }

    @Override
    public void deleteByChatId(String chatId) {
        lambdaUpdate()
                .eq(Repository::getChatId, chatId)
                .remove();
    }

    @Override
    public void updateSessionTitle(String chatId, String sessionTitle) {
        Repository repository = new Repository();
        repository.setSessionTitle(sessionTitle);
        lambdaUpdate()
                .eq(Repository::getChatId, chatId)
                .update(repository);
    }

    @Override
    public SessionVO createChat(String type) {
        // 生成唯一的会话ID和默认的会话标题的vo对象
        SessionUtilVO sessionUtilsVO = SessionUtils.generateChatId(UserContext.getUserId());
        // 保存会话
        save(type, sessionUtilsVO.getChatId(), sessionUtilsVO.getSessionTitle());
        SessionVO sessionVO = new SessionVO();
        sessionVO.setChatId(sessionUtilsVO.getChatId());
        sessionVO.setSessionTitle(sessionUtilsVO.getSessionTitle());
        sessionVO.setType(type);
        return sessionVO;
    }
}

