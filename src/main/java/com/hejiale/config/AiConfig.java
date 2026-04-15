package com.hejiale.config;

import com.hejiale.controller.LinkController;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.hejiale.common.constants.SystemPromptConstants.SERVICE_PROMPT;

@Configuration
@AllArgsConstructor
public class AiConfig {

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder().build();
    }


    @Bean
    public ChatClient chatClient(OpenAiChatModel openAiChatModel, ChatMemory chatMemory) {
        return ChatClient
                .builder(openAiChatModel)
                .defaultOptions(ChatOptions.builder().model("qwen3.5-omni-plus").build()) // 重新设置使用的模型，覆盖yaml配置中的默认模型
                .defaultSystem("You are a helpful assistant.")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @Bean
    public ChatClient serviceChatClient(OpenAiChatModel openAiChatModel, ChatMemory chatMemory, LinkController linkController) {
        return ChatClient
                .builder(openAiChatModel)
                .defaultSystem(SERVICE_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(linkController)
                .build();
    }

    @Bean
    public ChatClient pdfChatClient(OpenAiChatModel openAiChatModel, ChatMemory chatMemory, VectorStore vectorStore) {
        return ChatClient
                .builder(openAiChatModel)
                .defaultSystem("你是一个有帮助的助手，用户会上传PDF文档，你需要根据这些文档来回答用户的问题。")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .similarityThreshold(0.6)
                                        .topK(2)
                                        .build()
                                )
                                .build()
                )
                .build();
    }
}
