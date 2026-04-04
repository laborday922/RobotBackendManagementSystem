import request from '@/utils/request'

// 查询支持的API列表
export function listApi(query) {
  return request({
    url: '/app/api/list',
    method: 'get',
    params: query
  })
}

// 查询支持的API详细
export function getApi(id) {
  return request({
    url: '/app/api/' + id,
    method: 'get'
  })
}

// 新增支持的API
export function addApi(data) {
  return request({
    url: '/app/api',
    method: 'post',
    data: data
  })
}

// 修改支持的API
export function updateApi(data) {
  return request({
    url: '/app/api',
    method: 'put',
    data: data
  })
}

// 删除支持的API
export function delApi(id) {
  return request({
    url: '/app/api/' + id,
    method: 'delete'
  })
}
