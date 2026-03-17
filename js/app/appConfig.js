import request from '@/utils/request'

// 查询应用配置列表
export function listAppConfig(query) {
  return request({
    url: '/app/appConfig/list',
    method: 'get',
    params: query
  })
}

// 查询应用配置详细
export function getAppConfig(id) {
  return request({
    url: '/app/appConfig/' + id,
    method: 'get'
  })
}

// 新增应用配置
export function addAppConfig(data) {
  return request({
    url: '/app/appConfig',
    method: 'post',
    data: data
  })
}

// 修改应用配置
export function updateAppConfig(data) {
  return request({
    url: '/app/appConfig',
    method: 'put',
    data: data
  })
}

// 删除应用配置
export function delAppConfig(id) {
  return request({
    url: '/app/appConfig/' + id,
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
