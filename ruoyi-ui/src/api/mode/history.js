import request from '@/utils/request'

// 查询历史记录列表
export function listHistory(query) {
  return request({
    url: '/system/history/list',
    method: 'get',
    params: query
  })
}

// 查询历史记录详细
export function getHistory(historyId) {
  return request({
    url: '/system/history/' + historyId,
    method: 'get'
  })
}

// 新增历史记录
export function addHistory(data) {
  return request({
    url: '/system/history',
    method: 'post',
    data: data
  })
}

// 删除历史记录
export function delHistory(historyId) {
  return request({
    url: '/system/history/' + historyId,
    method: 'delete'
  })
}

// 清空历史记录
export function clearHistory() {
  return request({
    url: '/system/history/clear',
    method: 'delete'
  })
}
