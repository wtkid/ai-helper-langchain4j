// API配置
export const API_CONFIG = {
  // 默认API地址，可以通过环境变量或配置文件修改
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:8081/api',
  chatEndpoint: '/ai/chat'
}

// 获取完整的聊天接口URL
export function getChatUrl() {
  return `${API_CONFIG.baseURL}${API_CONFIG.chatEndpoint}`
}
