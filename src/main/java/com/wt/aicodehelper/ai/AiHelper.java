//package com.wt.aicodehelper.ai;
//
//import dev.langchain4j.data.message.AiMessage;
//import dev.langchain4j.data.message.SystemMessage;
//import dev.langchain4j.data.message.UserMessage;
//import dev.langchain4j.model.chat.ChatModel;
//import dev.langchain4j.model.chat.response.ChatResponse;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//@Service
//@Slf4j
//public class AiHelper {
//
//    @Resource
//    private ChatModel qwenChatModel;
//
//    private static final String SYSTEM_MESSAGE = """
//    你是数据库专家，擅长从数据上去帮助用户解决系统在使用过程中遇到的一切问题，并给出建议。重点关注以下几点：
//        1. 分析问题可能产生的原因
//        2. 按照你的猜测，查询对应的数据并确认
//        请用简洁易懂的语言回答，帮助用户更好的使用系统。
//            """;
//
//    public String chat(String message) {
//        SystemMessage systemMessage = SystemMessage.from(SYSTEM_MESSAGE);
//        UserMessage userMessage = UserMessage.from(message);
//        ChatResponse chatResponse = qwenChatModel.chat(systemMessage, userMessage);
//        AiMessage aiMessage = chatResponse.aiMessage();
//        log.info("AI 输出：" + aiMessage.toString());
//        return aiMessage.text();
//    }
//
//    public String chatWithMessage(UserMessage userMessage) {
//        ChatResponse chatResponse = qwenChatModel.chat(userMessage);
//        AiMessage aiMessage = chatResponse.aiMessage();
//        log.info("AI 输出：" + aiMessage.toString());
//        return aiMessage.text();
//    }
//}
