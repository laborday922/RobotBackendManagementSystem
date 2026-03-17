import request from '@/utils/request'

// 查询机器人位置历史信息列表
export function listHistory(query) {
  return request({
    url: '/robots/history/list',
    method: 'get',
    params: query
  })
}

// 查询机器人位置历史信息详细
export function getHistory(id) {
  return request({
    url: '/robots/history/' + id,
    method: 'get'
  })
}

// 新增机器人位置历史信息
export function addHistory(data) {
  return request({
    url: '/robots/history',
    method: 'post',
    data: data
  })
}

// 修改机器人位置历史信息
export function updateHistory(data) {
  return request({
    url: '/robots/history',
    method: 'put',
    data: data
  })
}

// 删除机器人位置历史信息
export function delHistory(id) {
  return request({
    url: '/robots/history/' + id,
    method: 'delete'
  })
}
