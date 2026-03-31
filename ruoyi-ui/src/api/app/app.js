import request from '@/utils/request'

// 查询应用能力参数定义列表
export function listAppParam(query) {
  return request({
    url: '/app/param/list',
    method: 'get',
    params: query
  })
}

// 查询应用能力参数定义详细
export function getAppParam(id) {
  return request({
    url: '/app/param/' + id,
    method: 'get'
  })
}

// 新增应用能力参数定义
export function addAppParam(data) {
  return request({
    url: '/app/param',
    method: 'post',
    data: data
  })
}

// 修改应用能力参数定义
export function updateAppParam(data) {
  return request({
    url: '/app/param',
    method: 'put',
    data: data
  })
}

// 删除应用能力参数定义
export function delAppParam(id) {
  return request({
    url: '/app/param/' + id,
    method: 'delete'
  })
}

// 导出应用能力参数定义
export function exportAppParam(query) {
  return request({
    url: '/app/param/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}

// 查询支持的API列表
export function listAppApi(query) {
  return request({
    url: '/app/api/list',
    method: 'get',
    params: query
  })
}

// 查询支持的API详细
export function getAppApi(id) {
  return request({
    url: '/app/api/' + id,
    method: 'get'
  })
}

// 新增支持的API
export function addAppApi(data) {
  return request({
    url: '/app/api',
    method: 'post',
    data: data
  })
}

// 修改支持的API
export function updateAppApi(data) {
  return request({
    url: '/app/api',
    method: 'put',
    data: data
  })
}

// 删除支持的API
export function delAppApi(id) {
  return request({
    url: '/app/api/' + id,
    method: 'delete'
  })
}

// 导出支持的API
export function exportAppApi(query) {
  return request({
    url: '/app/api/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}

// 查询应用库列表
export function listAppLibrary(query) {
  return request({
    url: '/app/appLibrary/list',
    method: 'get',
    params: query
  })
}

// 查询应用库详细
export function getAppLibrary(id) {
  return request({
    url: '/app/appLibrary/' + id,
    method: 'get'
  })
}

// 新增应用库
export function addAppLibrary(data) {
  return request({
    url: '/app/appLibrary',
    method: 'post',
    data: data
  })
}

// 修改应用库
export function updateAppLibrary(data) {
  return request({
    url: '/app/appLibrary',
    method: 'put',
    data: data
  })
}

// 删除应用库
export function delAppLibrary(id) {
  return request({
    url: '/app/appLibrary/' + id,
    method: 'delete'
  })
}

// 导出应用库
export function exportAppLibrary(query) {
  return request({
    url: '/app/appLibrary/export',
    method: 'post',
    params: query,
    responseType: 'blob'
  })
}
