import request from '@/utils/request'

// 查询应用更新记录列表
export function listAppUpdate(query) {
  return request({
    url: '/app/appUpdate/list',
    method: 'get',
    params: query
  })
}

// 查询应用更新记录详细
export function getAppUpdate(id) {
  return request({
    url: '/app/appUpdate/' + id,
    method: 'get'
  })
}

// 新增应用更新记录
export function addAppUpdate(data) {
  return request({
    url: '/app/appUpdate',
    method: 'post',
    data: data
  })
}

// 修改应用更新记录
export function updateAppUpdate(data) {
  return request({
    url: '/app/appUpdate',
    method: 'put',
    data: data
  })
}

// 删除应用更新记录
export function delAppUpdate(id) {
  return request({
    url: '/app/appUpdate/' + id,
    method: 'delete'
  })
}

export function listAppLibrary(query) {
  return request({
    url: '/app/appLibrary/list',
    method: 'get',
    params: query
  })
}