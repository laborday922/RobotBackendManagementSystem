import request from '@/utils/request'

/**
 * 获取历史执行记录列表
 */
export function getHistoryList() {
  return request({
    url: '/clean/history/list',
    method: 'get'
  })
}

/**
 * 创建执行记录（保存配置）
 * @param {Object} data 配置参数
 */
export function createHistory(data) {
  return request({
    url: '/clean/history/create',
    method: 'post',
    data: data
  })
}

/**
 * 立即执行清洗任务
 * @param {Number} id 记录ID
 */
export function executeTask(id) {
  return request({
    url: `/clean/execute/clean/${id}`,
    method: 'post'
  })
}
