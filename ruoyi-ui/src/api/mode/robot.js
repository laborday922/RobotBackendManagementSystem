import request from '@/utils/request'

// 查询机器人列表
export function listRobot(query) {
  return request({
    url: '/mode/robots/list',  // 修改这里
    method: 'get',
    params: query
  })
}

// 查询机器人详细
export function getRobot(robotId) {
  return request({
    url: '/mode/robots/' + robotId,  // 修改这里
    method: 'get'
  })
}

// 批量重启机器人
export function batchRestart(data) {
  return request({
    url: '/mode/robots/batchRestart',  // 修改这里
    method: 'put',
    data: data,
    timeout: 60000
  })
}

// 紧急停止
export function emergencyStop(data) {
  return request({
    url: '/mode/robots/emergencyStop',  // 修改这里
    method: 'put',
    data: data,
    timeout: 30000
  })
}

// 刷新状态
export function refreshStatus(data) {
  return request({
    url: '/mode/robots/refreshStatus',  // 修改这里
    method: 'put',
    data: data,
    timeout: 30000
  })
}

// 测试告警
export function testAlert(data) {
  return request({
    url: '/mode/robots/testAlert',  // 修改这里
    method: 'put',
    data: data,
    timeout: 30000
  })
}

// 清除告警
export function clearAlerts(data) {
  return request({
    url: '/mode/robots/clearAlerts',  // 修改这里
    method: 'put',
    data: data,
    timeout: 30000
  })
}

// 切换待机模式
export function standbyMode(data) {
  return request({
    url: '/mode/robots/standbyMode',  // 修改这里
    method: 'put',
    data: data,
    timeout: 30000
  })
}

// 切换维护模式
export function maintenanceMode(data) {
  return request({
    url: '/mode/robots/maintenanceMode',  // 修改这里
    method: 'put',
    data: data,
    timeout: 30000
  })
}

// 切换充电模式
export function chargeMode(data) {
  return request({
    url: '/mode/robots/chargeMode',  // 修改这里
    method: 'put',
    data: data,
    timeout: 30000
  })
}

// 返回充电
export function returnCharge(data) {
  return request({
    url: '/mode/robots/returnCharge',  // 修改这里
    method: 'put',
    data: data,
    timeout: 30000
  })
}

// 获取在线机器人列表
export function listOnlineRobot() {
  return request({
    url: '/mode/robots/online/list',  // 修改这里
    method: 'get'
  })
}

// 获取低电量机器人列表
export function listLowBatteryRobot(threshold) {
  return request({
    url: '/mode/robots/lowBattery/list',  // 修改这里
    method: 'get',
    params: { threshold: threshold }
  })
}

// 更新机器人当前模式
export function updateRobotMode(data) {
  return request({
    url: '/mode/robots/updateMode',  // 修改这里
    method: 'put',
    data: data
  })
}

// 批量更新机器人模式
export function batchUpdateRobotMode(data) {
  return request({
    url: '/mode/robots/batchUpdateMode',  // 修改这里
    method: 'put',
    data: data
  })
}

// ==================== 模式配置相关API ====================

// 保存机器人模式配置
export function saveRobotModeConfig(data) {
  return request({
    url: '/mode/robots/saveModeConfig',  // 修改这里
    method: 'post',
    data: data
  })
}

// 获取机器人模式配置
export function getRobotModeConfig(robotId, modeId) {
  return request({
    url: '/mode/robots/modeConfig',  // 修改这里
    method: 'get',
    params: { robotId, modeId }
  })
}

// 批量保存机器人模式配置
export function batchSaveRobotModeConfig(data) {
  return request({
    url: '/mode/robots/batchSaveModeConfig',  // 修改这里
    method: 'post',
    data: data
  })
}

// 删除机器人模式配置
export function deleteRobotModeConfig(robotId, modeId) {
  return request({
    url: '/mode/robots/deleteModeConfig',  // 修改这里
    method: 'delete',
    params: { robotId, modeId }
  })
}

// 复制机器人模式配置
export function copyRobotModeConfig(data) {
  return request({
    url: '/mode/robots/copyModeConfig',  // 修改这里
    method: 'post',
    data: data
  })
}

// 导出机器人数据
export function exportRobot(query) {
  return request({
    url: '/mode/robots/export',  // 修改这里
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
