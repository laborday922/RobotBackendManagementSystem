import request from '@/utils/request'

// 获取点位列表
export function getPointList(params) {
  return request({
    url: '/func/point/list',
    method: 'get',
    params: params
  })
}

// 获取点位详情
export function getPoint(pointId) {
  return request({
    url: '/func/point/' + pointId,
    method: 'get'
  })
}

// 获取点位播报配置
export function getPointVoiceConfig(pointId) {
  return request({
    url: '/func/point/voice/' + pointId,
    method: 'get'
  })
}

// 保存点位播报配置
export function savePointVoiceConfig(data) {
  return request({
    url: '/func/point/voice/save',
    method: 'post',
    data: data
  })
}

// 根据机器人ID获取点位播报配置列表（用于机器人端同步）
export function getPointVoiceListByRobot(robotId) {
  return request({
    url: '/func/point/voice/listByRobot',
    method: 'get',
    params: { robotId: robotId }
  })
}

// 根据地图ID获取点位播报配置列表
export function getPointVoiceListByMap(mapId) {
  return request({
    url: '/func/point/voice/listByMap',
    method: 'get',
    params: { mapId: mapId }
  })
}

// 新增点位
export function addPoint(data) {
  return request({
    url: '/func/point',
    method: 'post',
    data: data
  })
}

// 修改点位
export function updatePoint(data) {
  return request({
    url: '/func/point',
    method: 'put',
    data: data
  })
}

// 删除点位
export function deletePoint(pointId) {
  return request({
    url: '/func/point/' + pointId,
    method: 'delete'
  })
}

// 批量删除点位
export function deletePoints(pointIds) {
  return request({
    url: '/func/point/batch',
    method: 'delete',
    data: pointIds
  })
}
