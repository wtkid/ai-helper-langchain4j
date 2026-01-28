package com.wt.aicodehelper.ai;

import com.wt.aicodehelper.ai.tools.InterviewQuestionTool;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiHelperServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    @Resource
    private ContentRetriever contentRetriever;

//    @Resource
//    private McpToolProvider mcpToolProvider;

    @Bean
    public AiHelperService aiCodeHelperService() {
        // 会话记忆
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
        // 构造 AI Service
        AiHelperService aiHelperService = AiServices.builder(AiHelperService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemory(chatMemory)
                .chatMemoryProvider(memoryId ->
                        MessageWindowChatMemory.withMaxMessages(10)) // 每个会话独立存储
                .contentRetriever(contentRetriever) // RAG 检索增强生成
                .tools(new InterviewQuestionTool()) // 工具调用
//                .toolProvider(mcpToolProvider) // MCP 工具调用
                .build();
        return aiHelperService;
    }
}
