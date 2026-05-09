import request from '@/utils/request'
import { getDynamicParams } from '@/api/taskmgt/taskmgt'

// ========== 点位基础 CRUD ==========
export function getPointList(params) {
  return request({
    url: '/func/point/list',
    method: 'get',
    params: params
  })
}

export function getPoint(pointId) {
  return request({
    url: '/func/point/' + pointId,
    method: 'get'
  })
}

export function addPoint(data) {
  return request({
    url: '/func/point',
    method: 'post',
    data: data
  })
}

export function updatePoint(data) {
  return request({
    url: '/func/point',
    method: 'put',
    data: data
  })
}

export function deletePoint(pointId) {
  return request({
    url: '/func/point/' + pointId,
    method: 'delete'
  })
}

export function deletePoints(pointIds) {
  return request({
    url: '/func/point/batch',
    method: 'delete',
    data: pointIds
  })
}

// ========== 点位播报配置 ==========
export function getPointVoiceConfig(pointId) {
  return request({
    url: '/func/point/voice/' + pointId,
    method: 'get'
  })
}

export function savePointVoiceConfig(data) {
  return request({
    url: '/func/point/voice/save',
    method: 'post',
    data: data
  })
}

export function getPointVoiceListByRobot(robotId) {
  return request({
    url: '/func/point/voice/listByRobot',
    method: 'get',
    params: { robotId: robotId }
  })
}

export function getPointVoiceListByMap(mapId) {
  return request({
    url: '/func/point/voice/listByMap',
    method: 'get',
    params: { mapId: mapId }
  })
}

// ========== 从机器人获取点位（复用任务模块的动态参数接口）==========
/**
 * 从机器人获取点位选项（与任务模块“目的地”完全一致）
 * @param {Number} robotId 机器人ID
 * @returns {Promise} 返回 { code, data: DynamicParamVo[] }
 */
export function getPointOptionsFromRobot(robotId) {
  // apiId=2 对应 robot.navigate 的 position 参数（valueSource = "DYNAMIC"）
  return getDynamicParams(2, { robotId })
}

// 注：原有的 syncPositionsFromRobot 可以保留或丢弃，现在改用上面的统一接口
/**
 * 根据机器人ID直接获取点位列表
 * @param {number} robotId 机器人ID
 */
export function getPointsByRobotId(robotId) {
  return request({
    url: '/func/point/list/byRobot',
    method: 'get',
    params: { robotId: robotId }
  })
}
