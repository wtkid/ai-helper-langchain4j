<template>
  <div class="chat-container">
    <div class="chat-header">
      <h1>问题排查助手</h1>
      <div class="chat-info">
        <span>会话ID: {{ memoryId }}</span>
      </div>
    </div>

    <div class="chat-messages" ref="messagesContainer">
      <div
        v-for="(msg, index) in messages"
        :key="index"
        :class="['message', msg.type === 'user' ? 'message-user' : 'message-ai']"
      >
        <div class="message-avatar" v-if="msg.type === 'ai'">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="4" y="6" width="16" height="12" rx="2" fill="currentColor"/>
            <circle cx="9" cy="11" r="1.5" fill="white"/>
            <circle cx="15" cy="11" r="1.5" fill="white"/>
            <rect x="8" y="14" width="8" height="2" rx="1" fill="white"/>
            <path d="M12 2L10 6H14L12 2Z" fill="currentColor"/>
          </svg>
        </div>
        <div class="message-avatar user-avatar" v-else :style="{ background: userAvatarColor }">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <circle cx="12" cy="8" r="4" fill="currentColor"/>
            <path d="M6 21c0-3.31 2.69-6 6-6s6 2.69 6 6" fill="currentColor"/>
          </svg>
        </div>
        <div class="message-content">
          <div class="message-text" v-html="formatMessage(msg.content)"></div>
          <div class="message-time" v-if="msg.content && msg.content.trim()">{{ formatTime(msg.timestamp) }}</div>
        </div>
      </div>
      <div v-if="isLoading" class="message message-ai">
        <div class="message-avatar">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="4" y="6" width="16" height="12" rx="2" fill="currentColor"/>
            <circle cx="9" cy="11" r="1.5" fill="white"/>
            <circle cx="15" cy="11" r="1.5" fill="white"/>
            <rect x="8" y="14" width="8" height="2" rx="1" fill="white"/>
            <path d="M12 2L10 6H14L12 2Z" fill="currentColor"/>
          </svg>
        </div>
        <div class="message-content">
          <div class="message-text typing-indicator">
            <span></span>
            <span></span>
            <span></span>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-input-container">
      <div class="input-wrapper">
        <textarea
          v-model="inputMessage"
          @keydown.enter.exact.prevent="sendMessage"
          @keydown.shift.enter.exact="handleShiftEnter"
          placeholder="请输入您的问题..."
          rows="1"
          class="chat-input"
          :disabled="isLoading"
        ></textarea>
        <button
          @click="sendMessage"
          :disabled="!inputMessage.trim() || isLoading || currentEventSource"
          class="send-button"
        >
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z" fill="currentColor"/>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { createSSEConnection } from './utils/sse.js'

// 响应式数据
const memoryId = ref(null)
const messages = ref([])
const inputMessage = ref('')
const isLoading = ref(false)
const messagesContainer = ref(null)
const userAvatarColor = ref('')
let currentEventSource = null

// 随机用户头像颜色（生成一个随机颜色）
const avatarColors = [
  '#FF6B6B', '#4ECDC4', '#45B7D1', '#FFA07A', '#98D8C8',
  '#F7DC6F', '#BB8FCE', '#85C1E2', '#F8B739', '#52BE80'
]

function getRandomAvatarColor() {
  return avatarColors[Math.floor(Math.random() * avatarColors.length)]
}

// 生成随机聊天室ID（int类型）
function generateMemoryId() {
  // 生成1到2147483647之间的随机整数
  return Math.floor(Math.random() * 2147483647) + 1
}

// 格式化消息内容（支持换行）
function formatMessage(content) {
  if (!content) return ''
  return content
    .replace(/\n/g, '<br>')
    .replace(/ /g, '&nbsp;')
}

// 格式化时间
function formatTime(timestamp) {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 滚动到底部
function scrollToBottom() {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 发送消息
function sendMessage() {
  const message = inputMessage.value.trim()
  
  // 防止重复提交：检查消息是否为空、是否正在加载、是否已有连接
  if (!message || isLoading.value || currentEventSource) {
    return
  }

  // 立即设置加载状态，防止重复调用
  isLoading.value = true

  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: message,
    timestamp: Date.now()
  })

  // 清空输入框
  inputMessage.value = ''
  scrollToBottom()

  // 创建AI消息占位符
  const aiMessageIndex = messages.value.length
  messages.value.push({
    type: 'ai',
    content: '',
    timestamp: Date.now()
  })

  scrollToBottom()

  // 关闭之前的连接（如果有，双重保险）
  if (currentEventSource) {
    currentEventSource.close()
    currentEventSource = null
  }

  // 创建SSE连接
  let errorHandled = false
  currentEventSource = createSSEConnection(
    memoryId.value,
    message,
    // onMessage - 接收消息片段
    (chunk) => {
      if (messages.value[aiMessageIndex]) {
        messages.value[aiMessageIndex].content += chunk
        scrollToBottom()
      }
    },
    // onError - 错误处理
    (error) => {
      // 防止重复触发错误处理
      if (errorHandled) {
        return
      }
      errorHandled = true
      console.error('SSE错误:', error)
      if (messages.value[aiMessageIndex]) {
        const currentContent = messages.value[aiMessageIndex].content
        // 如果还没有内容，或者内容中不包含错误信息，才添加错误信息
        if (!currentContent || !currentContent.includes('[错误:')) {
          messages.value[aiMessageIndex].content += (currentContent ? '\n\n' : '') + '[错误: 连接中断，请重试]'
        }
      }
      isLoading.value = false
      currentEventSource = null
    },
    // onComplete - 完成
    () => {
      isLoading.value = false
      currentEventSource = null
    }
  )
}

// 处理Shift+Enter（换行）
function handleShiftEnter() {
  // 允许换行，不做任何处理
}

// 监听消息变化，自动滚动
watch(
  () => messages.value.length,
  () => {
    scrollToBottom()
  }
)

// 组件挂载时初始化
onMounted(() => {
  memoryId.value = generateMemoryId()
  userAvatarColor.value = getRandomAvatarColor()
  console.log('生成的聊天室ID:', memoryId.value)
  
  // 添加欢迎消息
  messages.value.push({
    type: 'ai',
    content: '您好！我是问题排查助手，可以帮助您解决系统使用过程中的相关问题。请告诉我您遇到的问题。',
    timestamp: Date.now()
  })
})

// 组件卸载时清理连接
onUnmounted(() => {
  if (currentEventSource) {
    currentEventSource.close()
    currentEventSource = null
  }
})
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f7;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.chat-header {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  padding: 1rem 1.5rem;
  border-bottom: 0.5px solid rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 100;
}

.chat-header h1 {
  font-size: 1.25rem;
  color: #1d1d1f;
  font-weight: 600;
  letter-spacing: -0.01em;
}

.chat-info {
  font-size: 0.8125rem;
  color: #86868b;
  font-weight: 400;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  scroll-behavior: smooth;
}

.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}

.message {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  animation: fadeIn 0.4s cubic-bezier(0.16, 1, 0.3, 1);
  margin-bottom: 0.25rem;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-user {
  flex-direction: row-reverse;
}

.message-ai {
  flex-direction: row;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 18px;
  background: #007aff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: white;
  box-shadow: 0 2px 8px rgba(0, 122, 255, 0.25);
}

.message-avatar svg {
  width: 20px;
  height: 20px;
}

.user-avatar {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.user-avatar svg {
  width: 22px;
  height: 22px;
}

.message-content {
  max-width: 70%;
  padding: 0.875rem 1rem;
  border-radius: 20px;
  word-wrap: break-word;
  position: relative;
}

.message-user .message-content {
  background: #007aff;
  color: white;
  border-bottom-right-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 122, 255, 0.2);
}

.message-ai .message-content {
  background: #ffffff;
  color: #1d1d1f;
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 0.5px solid rgba(0, 0, 0, 0.05);
}

.message-text {
  line-height: 1.5;
  font-size: 0.9375rem;
  white-space: pre-wrap;
  word-break: break-word;
  letter-spacing: -0.01em;
}

.message-time {
  font-size: 0.6875rem;
  opacity: 0.6;
  margin-top: 0.375rem;
  font-weight: 400;
}

.typing-indicator {
  display: flex;
  gap: 0.375rem;
  padding: 0.25rem 0;
  align-items: center;
}

.typing-indicator span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #86868b;
  animation: typing 1.2s infinite ease-in-out;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.15s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  30% {
    transform: translateY(-6px);
    opacity: 1;
  }
}

.chat-input-container {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: saturate(180%) blur(20px);
  -webkit-backdrop-filter: saturate(180%) blur(20px);
  padding: 1rem 1.5rem;
  border-top: 0.5px solid rgba(0, 0, 0, 0.1);
  position: sticky;
  bottom: 0;
  z-index: 100;
}

.input-wrapper {
  display: flex;
  gap: 0.75rem;
  align-items: flex-end;
  max-width: 1200px;
  margin: 0 auto;
}

.chat-input {
  flex: 1;
  padding: 0.75rem 1rem;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 20px;
  font-size: 0.9375rem;
  font-family: inherit;
  resize: none;
  outline: none;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  max-height: 120px;
  background: rgba(142, 142, 147, 0.12);
  color: #1d1d1f;
  line-height: 1.5;
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.chat-input::-webkit-scrollbar {
  display: none;
}

.chat-input:focus {
  border-color: #007aff;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 0 0 4px rgba(0, 122, 255, 0.1);
}

.chat-input:disabled {
  background: rgba(142, 142, 147, 0.08);
  cursor: not-allowed;
  opacity: 0.6;
}

.chat-input::placeholder {
  color: #86868b;
}

.send-button {
  width: 36px;
  height: 36px;
  padding: 0;
  background: #007aff;
  color: white;
  border: none;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 122, 255, 0.3);
}

.send-button svg {
  width: 18px;
  height: 18px;
}

.send-button:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 122, 255, 0.4);
  background: #0051d5;
}

.send-button:active:not(:disabled) {
  transform: scale(0.95);
}

.send-button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  background: #86868b;
  box-shadow: none;
}

@media (max-width: 768px) {
  .message-content {
    max-width: 80%;
  }

  .chat-header h1 {
    font-size: 1.125rem;
  }

  .chat-header {
    padding: 0.875rem 1rem;
  }

  .chat-messages {
    padding: 1rem;
    gap: 1rem;
  }

  .chat-input-container {
    padding: 0.875rem 1rem;
  }

  .message-avatar {
    width: 32px;
    height: 32px;
    border-radius: 16px;
  }

  .message-avatar svg {
    width: 18px;
    height: 18px;
  }

  .user-avatar svg {
    width: 20px;
    height: 20px;
  }
}
</style>
