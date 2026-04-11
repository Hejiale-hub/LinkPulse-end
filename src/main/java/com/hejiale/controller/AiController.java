package com.hejiale.controller;

import com.hejiale.domain.vo.AiMessageVO;
import com.hejiale.domain.vo.SessionVO;
import com.hejiale.service.IRepositoryService;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;


@RestController
@RequestMapping("/ai")
@AllArgsConstructor
public class AiController {
    private final ChatClient chatClient;
    private final IRepositoryService repositoryService;
    private final ChatMemory chatMemory;

    /**
     * 创建会话（新建对话窗口）
     */
    @GetMapping("/createChat")
    public SessionVO createChat(@RequestParam String type) {
        SessionVO sessionVO = repositoryService.createChat(type);
        return sessionVO;
    }

    /**
     * 开始发送chat模型对话
     * @param prompt 用户输入的对话内容
     * @param chatId 会话ID，用于区分不同的对话会话
     * @return 模型生成的对话回复
     */
    @PostMapping("/chat")
    public List<AiMessageVO> chat(String prompt, String chatId) {
        // 调用模型对话
        String content = chatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CONVERSATION_ID, chatId))
                .call()
                .content();
        AiMessageVO aiMessageVO = new AiMessageVO();
        aiMessageVO.setContent(content);
        aiMessageVO.setRole("assistant");
        return List.of(aiMessageVO);
    }

    /**
     * 获取会话ID列表
     * @return 会话ID列表
     */
    @GetMapping("/history")
    public List<SessionVO> getChatIds() {
        return repositoryService.getChatIds();
    }

    /**
     * 获取会话详细对话内容
     * @param chatId 会话ID
     */
    @GetMapping("/history/detail/{chatId}")
    public List<AiMessageVO> getChatDetail(@PathVariable("chatId") String chatId) {
        List<Message> messages = chatMemory.get(chatId);
        return messages.stream()
                .map(AiMessageVO::new)
                .toList();
    }

    /**
     * 删除会话ID
     * @param chatId 会话ID
     */
    @GetMapping("/history/delete/{chatId}")
    public void deleteChatId(@PathVariable("chatId") String chatId) {
        repositoryService.deleteByChatId(chatId);
    }

    /**
     * 更新会话标题
     * @param chatId 会话ID
     * @param sessionTitle 新的会话标题
     */
    @PostMapping("/history/title")
    public void updateSessionTitle(@RequestParam String chatId, @RequestParam String sessionTitle) {
        repositoryService.updateSessionTitle(chatId, sessionTitle);
    }
}
