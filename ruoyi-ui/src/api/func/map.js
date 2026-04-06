import request from '@/utils/request'

// 获取地图列表
export function getMapList(params) {
  return request({
    url: '/func/map/list',
    method: 'get',
    params: params
  })
}

// 获取地图详情
export function getMap(mapId) {
  return request({
    url: '/func/map/' + mapId,
    method: 'get'
  })
}

// 新增地图
export function addMap(data) {
  return request({
    url: '/func/map',
    method: 'post',
    data: data
  })
}

// 修改地图
export function updateMap(data) {
  return request({
    url: '/func/map',
    method: 'put',
    data: data
  })
}

// 删除地图
export function delMap(mapId) {
  return request({
    url: '/func/map/' + mapId,
    method: 'delete'
  })
}

// 上传地图文件
export function uploadMap(data) {
  return request({
    url: '/func/map/upload',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 获取地图点位列表
export function getPointListByMap(mapId) {
  return request({
    url: '/func/map/points/' + mapId,
    method: 'get'
  })
}

// 获取地图图片（通过接口返回blob）
export function getMapImage(mapId) {
  return request({
    url: '/func/map/image/' + mapId,
    method: 'get',
    responseType: 'blob'
  })
}
