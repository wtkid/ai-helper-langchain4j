//package com.wt.aicodehelper.ai.mcp;
//
//import dev.langchain4j.mcp.McpToolProvider;
//import dev.langchain4j.mcp.client.DefaultMcpClient;
//import dev.langchain4j.mcp.client.McpClient;
//import dev.langchain4j.mcp.client.transport.McpTransport;
//import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class McpConfig {
//
//    @Bean
//    public McpToolProvider mcpToolProvider() {
//        // 和 MCP 服务通讯
//        McpTransport transport = new HttpMcpTransport.Builder()
//                .sseUrl("http://192.168.2.13:3000/sse")
////                .sseUrl("http://127.0.0.1:3000/sse")
//                .logRequests(true) // 开启日志，查看更多信息
//                .logResponses(true)
//                .build();
//        // 创建 MCP 客户端
//        McpClient mcpClient = new DefaultMcpClient.Builder()
//                .key("mysqlMcpClient")
//                .transport(transport)
//                .build();
//
////		McpTransport transport2 = new HttpMcpTransport.Builder()
////				.sseUrl("http://127.0.0.1:8080/mcp")
////				.logRequests(true) // 开启日志，查看更多信息
////				.logResponses(true)
////				.build();
////		// 创建 MCP 客户端
////		McpClient mcpClient2 = new DefaultMcpClient.Builder()
////				.key("translatorMcpClient")
////				.protocolVersion("1.1")
////				.transport(transport2)
////				.build();
//
//        // 从 MCP 客户端获取工具
//        McpToolProvider toolProvider = McpToolProvider.builder()
//                .mcpClients(mcpClient)
//                .build();
//        return toolProvider;
//    }
//}
