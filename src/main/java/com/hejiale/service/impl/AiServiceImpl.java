package com.hejiale.service.impl;

import com.hejiale.domain.vo.AiMessageVO;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

import static org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor.FILTER_EXPRESSION;
import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@AllArgsConstructor
@Service
public class AiServiceImpl implements com.hejiale.service.IAiService {
    private final ChatClient chatClient;
    private final ChatClient pdfChatClient;
    private final ChatClient serviceChatClient;

    // chat对话，支持文本和多模态输入
    @Override
    public List<AiMessageVO> chat(String prompt, String chatId, List<MultipartFile> files) {
        // 判断是否有文件上传，如果有则调用多模态模型进行处理
        String content;
        if (files != null && !files.isEmpty()) {
            // 多模态模型对话
            content = multiModelChat(prompt, chatId, files);
        }else {
            // 调用文本模型对话
            content = textModelChat(prompt, chatId);
        }
        AiMessageVO aiMessageVO = new AiMessageVO();
        aiMessageVO.setContent(content);
        aiMessageVO.setRole("assistant");
        return List.of(aiMessageVO);
    }

    // service对话，智能客服场景，调用预设了工具的模型进行对话
    @Override
    public List<AiMessageVO> service(String prompt, String chatId) {
        // 调用模型对话
        String content = serviceChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CONVERSATION_ID, chatId))
                .call()
                .content();
        AiMessageVO aiMessageVO = new AiMessageVO();
        aiMessageVO.setContent(content);
        aiMessageVO.setRole("assistant");
        return List.of(aiMessageVO);
    }

    // pdf对话，用户上传PDF文档，模型根据文档内容进行问答
    @Override
    public List<AiMessageVO> pdf(String prompt, String chatId) {
        // 调用模型对话
        String content = pdfChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CONVERSATION_ID, chatId))
                .advisors(a -> a.param(FILTER_EXPRESSION, "chatId == '" + chatId + "'"))
                .call()
                .content();
        AiMessageVO aiMessageVO = new AiMessageVO();
        aiMessageVO.setContent(content);
        aiMessageVO.setRole("assistant");
        return List.of(aiMessageVO);
    }

    // 纯文本对话
    private String textModelChat(String prompt, String chatId) {
        // 调用模型对话
        return chatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CONVERSATION_ID, chatId))
                .call()
                .content();
    }

    // 多模态对话，处理用户上传的文件并调用模型进行对话
    private String multiModelChat(String prompt, String chatId, List<MultipartFile> files) {
        // 解析文件
        List<Media> medias = files.stream()
                .map(file -> new Media(
                        MimeType.valueOf(Objects.requireNonNull(file.getContentType())),
                        file.getResource()
                        )
                )
                .toList();
        // 调用多模态模型
        return chatClient.prompt()
                .user(p -> p.text(prompt).media(medias.toArray(Media[]::new)))
                .advisors(a -> a.param(CONVERSATION_ID, chatId))
                .call()
                .content();
    }
}
