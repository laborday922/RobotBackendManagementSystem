import request from '@/utils/request'

/**
 * 获取定时任务列表（等待定时执行的任务）
 */
export function getTaskList() {
  return request({
    url: '/clean/history/list',
    method: 'get'
  })
}

/**
 * 新增定时任务
 * @param {Object} data 配置参数
 */
export function createTask(data) {
  return request({
    url: '/clean/history/create',
    method: 'post',
    data: data
  })
}

/**
 * 编辑定时任务
 * @param {Number} id 任务ID
 * @param {Object} data 配置参数
 */
export function updateTask(id, data) {
  return request({
    url: `/clean/history/${id}`,
    method: 'put',
    data: data
  })
}

/**
 * 删除定时任务
 * @param {Number} id 任务ID
 */
export function deleteTask(id) {
  return request({
    url: `/clean/history/${id}`,
    method: 'delete'
  })
}

/**
 * 手动立即执行（不落任务，只产生执行记录）
 * @param {Object} data { configJson, applyDataSource }
 */
export function executeManual(data) {
  return request({
    url: '/clean/execute/manual',
    method: 'post',
    data: data
  })
}

/**
 * 获取执行记录列表（定时 + 手动）
 */
export function getRecordList(params) {
  return request({
    url: '/clean/record/list',
    method: 'get',
    params
  })
}
