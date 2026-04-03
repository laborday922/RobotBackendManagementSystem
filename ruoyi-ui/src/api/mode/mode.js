import request from '@/utils/request'

// 查询模式列表
export function listMode(query) {
  return request({
    url: '/system/mode/list',
    method: 'get',
    params: query
  })
}

// 查询模式详细
export function getMode(modeId) {
  return request({
    url: '/system/mode/' + modeId,
    method: 'get'
  })
}

// 新增模式
export function addMode(data) {
  return request({
    url: '/system/mode',
    method: 'post',
    data: data
  })
}

// 修改模式
export function updateMode(data) {
  return request({
    url: '/system/mode',
    method: 'put',
    data: data
  })
}

// 删除模式
export function delMode(modeId) {
  return request({
    url: '/system/mode/' + modeId,
    method: 'delete'
  })
}

// 修改模式状态
export function changeModeStatus(data) {
  return request({
    url: '/system/mode/changeStatus',
    method: 'put',
    data: data
  })
}
