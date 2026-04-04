import request from '@/utils/request'

// 查询交互历史记录列表
export function listIntHistory(query) {
  return request({
    url: '/app/intHistory/list',
    method: 'get',
    params: query
  })
}

// 查询交互历史记录详细
export function getIntHistory(id) {
  return request({
    url: '/app/intHistory/' + id,
    method: 'get'
  })
}

// 新增交互历史记录
export function addIntHistory(data) {
  return request({
    url: '/app/intHistory',
    method: 'post',
    data: data
  })
}

// 修改交互历史记录
export function updateIntHistory(data) {
  return request({
    url: '/app/intHistory',
    method: 'put',
    data: data
  })
}

// 删除交互历史记录
export function delIntHistory(id) {
  return request({
    url: '/app/intHistory/' + id,
    method: 'delete'
  })
}
// 查询交互历史记录汇总
export function getSumOfIntHistory() {
  return request({
    url: '/app/intHistory/list/sumof' ,
    method: 'get'
  })
}