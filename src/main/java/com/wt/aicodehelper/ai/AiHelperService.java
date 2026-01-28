package com.wt.aicodehelper.ai;

import com.wt.aicodehelper.ai.guardrail.SafeInputGuardrail;
import dev.langchain4j.service.*;
import dev.langchain4j.service.guardrail.InputGuardrails;
import reactor.core.publisher.Flux;

import java.util.List;

//@InputGuardrails({SafeInputGuardrail.class})
public interface AiHelperService {

    @SystemMessage(fromResource = "system-prompt.txt")
    String chat(@MemoryId int memoryId, @UserMessage String userMessage);

    @SystemMessage(fromResource = "system-prompt.txt")
    Report chatForReport(String userMessage);

    record Report(String name, List<String> suggestionList) {
    }

    @SystemMessage(fromResource = "system-prompt.txt")
    Result<String> chatWithRag(String userMessage);

    // 流式对话
    Flux<String> chatStream(@MemoryId int memoryId, @UserMessage String userMessage);

}
