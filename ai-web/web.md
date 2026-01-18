你是一个专业的前端开发，请根据以下信息，帮我生成对应的前段代码，使用windows支持的命令来完成

## 需求

应用为<问题排查助手>，帮助用户解决系统使用过程中的相关问题，并给出建议

- 仅一个页面，页面风格为聊天室，上方是聊条记录，用户信息显示在右边，AI消息显示在左边
  下方为输入框
- AI 发送的消息头像使用机器人，用户发送的消息头像使用随机人物头像
- 进入页面时自定生成聊天室id，int类型，用来区分不同的回话
- 通过sse的方式调用chat接口，实时显示对话内容

## 技术选项

1. vue3
2. Axios

## 后端接口地址

http://192.168.2.10:8081/api，这个地址需要可配置

## Springboot 后端接口代码

```java
@GetMapping("/ai/chat")
public Flux<ServerSentEvent<String>> chat(int memoryId, String message) {
    return aiHelperService.chatStream(memoryId, message)
            .map(chunk -> ServerSentEvent.<String>builder()
                    .data(chunk)
                    .build());
}
```

