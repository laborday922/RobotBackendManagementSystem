import request from '@/utils/request'

// 查询应用能力参数定义列表
export function listParam(query) {
  return request({
    url: '/app/param/list',
    method: 'get',
    params: query
  })
}

// 查询应用能力参数定义详细
export function getParam(id) {
  return request({
    url: '/app/param/' + id,
    method: 'get'
  })
}

// 新增应用能力参数定义
export function addParam(data) {
  return request({
    url: '/app/param',
    method: 'post',
    data: data
  })
}

// 修改应用能力参数定义
export function updateParam(data) {
  return request({
    url: '/app/param',
    method: 'put',
    data: data
  })
}

// 删除应用能力参数定义
export function delParam(id) {
  return request({
    url: '/app/param/' + id,
    method: 'delete'
  })
}
