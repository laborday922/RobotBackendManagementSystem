import request from '@/utils/request'

// 查询机器人状态预警列表
export function listWarningsAll(query) {
  return request({
    url: '/robots/warnings/list',
    method: 'get',
    params: query
  })
}

export function listWarningsNo(query) {
  return request({
    url: '/robots/warnings/list/no',
    method: 'get',
    params: query
  })
}

export function listWarningsYes(query) {
  return request({
    url: '/robots/warnings/list/yes',
    method: 'get',
    params: query
  })
}

// 查询机器人状态预警详细
export function getWarnings(id) {
  return request({
    url: '/robots/warnings/' + id,
    method: 'get'
  })
}

// 新增机器人状态预警
export function addWarnings(data) {
  return request({
    url: '/robots/warnings',
    method: 'post',
    data: data
  })
}

// 处理机器人状态预警
export function dealWarnings(data) {
  return request({
    url: '/robots/warnings/deal',
    method: 'put',
    data: data
  })
}

// 删除机器人状态预警
export function delWarnings(id) {
  return request({
    url: '/robots/warnings/' + id,
    method: 'delete'
  })
}

// 导出机器人状态预警
export function exportWarnings(query) {
  return request({
    url: '/robots/warnings/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}