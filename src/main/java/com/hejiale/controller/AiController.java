package com.hejiale.controller;

import com.hejiale.domain.vo.AiMessageVO;
import com.hejiale.domain.vo.Result;
import com.hejiale.domain.vo.SessionVO;
import com.hejiale.service.IAiService;
import com.hejiale.service.IRepositoryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor.FILTER_EXPRESSION;
import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Slf4j
@RestController
@RequestMapping("/ai")
@AllArgsConstructor
public class AiController {
    private final IRepositoryService repositoryService;
    private final ChatMemory chatMemory;
    private final IAiService aiService;

    /**
     * 创建会话（新建对话窗口）
     */
    @GetMapping("/createChat")
    public Result<SessionVO> createChat(@RequestParam String type) {
        SessionVO sessionVO = repositoryService.createChat(type);
        return Result.success(sessionVO);
    }

    /**
     * 开始发送chat模型对话
     * @param prompt 用户输入的对话内容
     * @param chatId 会话ID，用于区分不同的对话会话
     * @return 模型生成的对话回复
     */
    @PostMapping("/chat")
    public Result<List<AiMessageVO>> chat(@RequestParam("prompt") String prompt,
                                          @RequestParam("chatId") String chatId,
                                          @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        List<AiMessageVO> aiMessageVOList = aiService.chat(prompt, chatId, files);
        return Result.success(aiMessageVOList);
    }

    /**
     * 开始发送service模型对话
     * @param prompt 用户输入的对话内容
     * @param chatId 会话ID，用于区分不同的对话会话
     * @return 模型生成的对话回复
     */
    @PostMapping("/service")
    public Result<List<AiMessageVO>> service(String prompt, String chatId) {
        List<AiMessageVO> aiMessageVOList = aiService.service(prompt, chatId);
        return Result.success(aiMessageVOList);
    }

    /**
     * 开始发送service模型对话
     * @return 解析结果
     */
    @PostMapping("/pdf")
    public Result<List<AiMessageVO>> pdf(String prompt, String chatId) {
        List<AiMessageVO> aiMessageVOList = aiService.pdf(prompt, chatId);
        return Result.success(aiMessageVOList);
    }

    /**
     * 上传PDF文件并解析保存
     * @param chatId 会话ID，用于区分不同的对话会话
     * @param file PDF文件
     * @return 文件访问地址
     */
    @PostMapping("/pdf/upload/{chatId}")
    public Result upload(@PathVariable String chatId, @RequestParam("file") MultipartFile file) throws IOException {
        repositoryService.savePdfFile(chatId, file);
        return Result.success();
    }

    /**
     * 获取会话ID列表
     * @return 会话ID列表
     */
    @GetMapping("/history")
    public Result<List<SessionVO>> getChatIds() {
        return Result.success(repositoryService.getChatIds());
    }

    /**
     * 获取会话详细对话内容
     * @param chatId 会话ID
     */
    @GetMapping("/history/detail/{chatId}")
    public Result<List<AiMessageVO>> getChatDetail(@PathVariable("chatId") String chatId) {
        List<Message> messages = chatMemory.get(chatId);
        List<AiMessageVO> AiMessageList = messages.stream()
                .map(AiMessageVO::new)
                .toList();
        return Result.success(AiMessageList);
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
