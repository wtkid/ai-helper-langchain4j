# 问题排查助手

一个基于 Vue3 的 AI 聊天应用，帮助用户解决系统使用过程中的相关问题。

## 功能特性

- 💬 聊天室风格界面，用户消息显示在右侧，AI消息显示在左侧
- 🔄 通过 SSE (Server-Sent Events) 实时接收AI回复
- 🆔 自动生成聊天室ID，区分不同会话
- ⚙️ 可配置的API地址

## 技术栈

- Vue 3
- Vite
- Axios
- SSE (Server-Sent Events)

## 安装和运行

### 1. 安装依赖

```bash
npm install
```

### 2. 配置API地址（可选）

默认API地址为 `http://192.168.2.10:8081/api`

如需修改，可以：

**方式1：创建 `.env` 文件**
```
VITE_API_BASE_URL=http://your-api-url:port/api
```

**方式2：直接修改 `src/config.js` 文件中的 `baseURL`**

### 3. 启动开发服务器

```bash
npm run dev
```

应用将在 `http://localhost:3000` 启动

### 4. 构建生产版本

```bash
npm run build
```

构建产物将输出到 `dist` 目录

## 项目结构

```
ai-web/
├── src/
│   ├── App.vue          # 主应用组件
│   ├── main.js          # 入口文件
│   ├── config.js        # 配置文件
│   └── utils/
│       └── sse.js       # SSE工具类
├── index.html           # HTML模板
├── vite.config.js       # Vite配置
├── package.json         # 项目配置
└── README.md           # 说明文档
```

## 后端接口说明

应用调用后端接口：`GET /ai/chat`

**参数：**
- `memoryId` (int): 聊天室ID
- `message` (string): 用户消息

**返回：**
- SSE流，实时返回AI回复内容

## 使用说明

1. 打开应用后，系统会自动生成一个聊天室ID
2. 在输入框中输入问题，按 Enter 发送（Shift+Enter 换行）
3. AI回复会通过SSE实时显示在左侧
4. 每次刷新页面会生成新的聊天室ID，开始新的会话

## 注意事项

- 确保后端接口支持CORS跨域请求
- 确保后端SSE接口正常工作
- 如果遇到连接问题，请检查API地址配置是否正确
