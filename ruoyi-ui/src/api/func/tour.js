import request from '@/utils/request'

// 获取讲解通用配置
export function getTourGeneral(robotId) {
  return request({
    url: '/func/tour/general/' + robotId,
    method: 'get'
  })
}

// 保存讲解通用配置
export function saveTourGeneral(data) {
  return request({
    url: '/func/tour/general',
    method: 'post',
    data: data
  })
}

// 获取讲解内容列表 - 修复：robotId 作为路径参数
export function getTourContentList(robotId, params) {
  return request({
    url: '/func/tour/content/list/' + robotId,
    method: 'get',
    params: params
  })
}

// 获取讲解内容详情
export function getTourContent(contentId) {
  return request({
    url: '/func/tour/content/' + contentId,
    method: 'get'
  })
}

// 保存讲解内容
export function saveTourContent(data) {
  return request({
    url: '/func/tour/content',
    method: 'post',
    data: data
  })
}

// 删除讲解内容
export function deleteTourContent(contentId) {
  return request({
    url: '/func/tour/content/' + contentId,
    method: 'delete'
  })
}

// 批量删除讲解内容
export function batchDeleteTourContents(contentIds) {
  return request({
    url: '/func/tour/content/batch',
    method: 'delete',
    data: contentIds,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

// 获取路线列表
export function getRouteList(params) {
  return request({
    url: '/func/tour/route/list',
    method: 'get',
    params: params
  })
}

// 获取路线详情
export function getRoute(routeId) {
  return request({
    url: '/func/tour/route/' + routeId,
    method: 'get'
  })
}

// 保存路线
export function saveRoute(data) {
  return request({
    url: '/func/tour/route',
    method: 'post',
    data: data
  })
}

// 删除路线
export function deleteRoute(routeId) {
  return request({
    url: '/func/tour/route/' + routeId,
    method: 'delete'
  })
}

// 导出路线
export function exportRoutes() {
  return request({
    url: '/func/tour/route/export',
    method: 'get'
  })
}

// 导入路线
export function importRoutes(data) {
  return request({
    url: '/func/tour/route/import',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}
