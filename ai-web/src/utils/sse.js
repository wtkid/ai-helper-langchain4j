import { getChatUrl } from '../config.js'

/**
 * 创建SSE连接并处理消息流
 * @param {number} memoryId - 聊天室ID
 * @param {string} message - 用户消息
 * @param {Function} onMessage - 接收到消息时的回调函数
 * @param {Function} onError - 错误处理回调函数
 * @param {Function} onComplete - 完成时的回调函数
 * @returns {EventSource} EventSource实例，可用于关闭连接
 */
export function createSSEConnection(memoryId, message, onMessage, onError, onComplete) {
  const url = new URL(getChatUrl())
  url.searchParams.append('memoryId', memoryId)
  url.searchParams.append('message', message)

  const eventSource = new EventSource(url.toString())
  let isCompleted = false
  let hasReceivedData = false
  let errorTriggered = false

  eventSource.onmessage = (event) => {
    try {
      const data = event.data
      if (data) {
        hasReceivedData = true
        onMessage(data)
      }
    } catch (error) {
      console.error('处理SSE消息时出错:', error)
      if (onError && !isCompleted && !errorTriggered) {
        errorTriggered = true
        onError(error)
      }
    }
  }

  eventSource.onerror = (error) => {
    // EventSource在连接关闭时会触发onerror
    // 需要区分是正常关闭（流结束）还是网络错误（需要重连）
    
    // 如果已经完成，直接返回，不再处理
    if (isCompleted) {
      return
    }

    if (eventSource.readyState === EventSource.CLOSED) {
      // 连接已关闭，这是正常的流结束
      isCompleted = true
      // 立即关闭连接，防止自动重连
      eventSource.close()
      if (onComplete) {
        onComplete()
      }
    } else if (eventSource.readyState === EventSource.CONNECTING) {
      // 正在重连
      // 如果已经接收过数据，说明服务器已经发送完数据并关闭了连接
      // 这是正常的结束，应该关闭连接而不是等待重连
      if (hasReceivedData) {
        // 已经接收过数据，说明流正常结束，服务器关闭了连接
        isCompleted = true
        eventSource.close()
        if (onComplete) {
          onComplete()
        }
      } else {
        // 从未接收过数据，可能是初始连接失败
        // 等待一下，看是否能连接成功
        setTimeout(() => {
          if (!isCompleted && eventSource.readyState === EventSource.CONNECTING) {
            // 仍然在连接中，可能是真的连接失败
            isCompleted = true
            eventSource.close()
            if (onError && !errorTriggered) {
              errorTriggered = true
              onError(error)
            }
          }
        }, 2000)
      }
    }
  }

  // 监听连接打开
  eventSource.onopen = () => {
    console.log('SSE连接已建立')
  }

  // 返回一个可以手动关闭的连接对象
  return {
    close: () => {
      if (!isCompleted) {
        isCompleted = true
        eventSource.close()
        if (onComplete) {
          onComplete()
        }
      }
    },
    eventSource: eventSource
  }
}
