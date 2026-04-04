import request from '@/utils/request'

// 查询机器人列表
export function listRobot(query) {
  return request({
    url: '/system/robot/list',
    method: 'get',
    params: query
  })
}

// 查询机器人详细
export function getRobot(robotId) {
  return request({
    url: '/system/robot/' + robotId,
    method: 'get'
  })
}

// 新增机器人
export function addRobot(data) {
  return request({
    url: '/system/robot',
    method: 'post',
    data: data
  })
}

// 修改机器人
export function updateRobot(data) {
  return request({
    url: '/system/robot',
    method: 'put',
    data: data
  })
}

// 删除机器人
export function delRobot(robotId) {
  return request({
    url: '/system/robot/' + robotId,
    method: 'delete'
  })
}

// 批量删除机器人
export function delRobots(robotIds) {
  return request({
    url: '/system/robot/batch',
    method: 'delete',
    data: robotIds
  })
}

// 更新机器人当前模式
export function updateRobotMode(data) {
  return request({
    url: '/system/robot/updateMode',
    method: 'put',
    data: data
  })
}

// 批量更新机器人模式
export function batchUpdateRobotMode(data) {
  return request({
    url: '/system/robot/batchUpdateMode',
    method: 'put',
    data: data
  })
}

// 批量重启机器人 - 增加超时时间
export function batchRestart(data) {
  return request({
    url: '/system/robot/batchRestart',
    method: 'put',
    data: data,
    timeout: 60000 // 60秒超时
  })
}

// 紧急停止
export function emergencyStop(data) {
  return request({
    url: '/system/robot/emergencyStop',
    method: 'put',
    data: data,
    timeout: 30000
  })
}

// 刷新状态
export function refreshStatus(data) {
  return request({
    url: '/system/robot/refreshStatus',
    method: 'put',
    data: data,
    timeout: 30000
  })
}

// 测试告警
export function testAlert(data) {
  return request({
    url: '/system/robot/testAlert',
    method: 'put',
    data: data,
    timeout: 30000
  })
}

// 清除告警
export function clearAlerts(data) {
  return request({
    url: '/system/robot/clearAlerts',
    method: 'put',
    data: data,
    timeout: 30000
  })
}

// 切换待机模式
export function standbyMode(data) {
  return request({
    url: '/system/robot/standbyMode',
    method: 'put',
    data: data,
    timeout: 30000
  })
}

// 切换维护模式
export function maintenanceMode(data) {
  return request({
    url: '/system/robot/maintenanceMode',
    method: 'put',
    data: data,
    timeout: 30000
  })
}

// 切换充电模式
export function chargeMode(data) {
  return request({
    url: '/system/robot/chargeMode',
    method: 'put',
    data: data,
    timeout: 30000
  })
}

// 获取在线机器人列表
export function listOnlineRobot() {
  return request({
    url: '/system/robot/online/list',
    method: 'get'
  })
}

// 获取低电量机器人列表
export function listLowBatteryRobot(threshold) {
  return request({
    url: '/system/robot/lowBattery/list',
    method: 'get',
    params: { threshold: threshold }
  })
}

// 获取机器人状态统计
export function getRobotStatusStats() {
  return request({
    url: '/system/robot/stats/status',
    method: 'get'
  })
}

// 获取机器人分组统计
export function getRobotGroupStats() {
  return request({
    url: '/system/robot/stats/group',
    method: 'get'
  })
}

// 导出机器人数据
export function exportRobot(query) {
  return request({
    url: '/system/robot/export',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}

// 导入机器人数据
export function importRobot(data) {
  return request({
    url: '/system/robot/import',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 下载导入模板
export function importTemplate() {
  return request({
    url: '/system/robot/importTemplate',
    method: 'get',
    responseType: 'blob'
  })
}

// 检查机器人编码是否唯一
export function checkRobotCodeUnique(robotCode) {
  return request({
    url: '/system/robot/checkCodeUnique',
    method: 'get',
    params: { robotCode: robotCode }
  })
}

// 检查机器人名称是否唯一
export function checkRobotNameUnique(robotName) {
  return request({
    url: '/system/robot/checkNameUnique',
    method: 'get',
    params: { robotName: robotName }
  })
}

// 获取机器人操作记录
export function listRobotOperation(query) {
  return request({
    url: '/system/robot/operation/list',
    method: 'get',
    params: query
  })
}

// 清除机器人告警
export function clearRobotAlerts(robotId) {
  return request({
    url: '/system/robot/clearAlerts/' + robotId,
    method: 'put'
  })
}

// 批量清除机器人告警
export function batchClearAlerts(robotIds) {
  return request({
    url: '/system/robot/batchClearAlerts',
    method: 'put',
    data: robotIds
  })
}

// ==================== 新增：机器人模式配置相关API ====================

/**
 * 保存机器人模式配置
 * @param {Object} data - 配置数据
 * @param {number} data.robotId - 机器人ID
 * @param {number} data.modeId - 模式ID
 * @param {Object} data.config - 配置参数（键值对）
 * @returns {Promise}
 */
export function saveRobotModeConfig(data) {
  return request({
    url: '/system/robot/saveModeConfig',
    method: 'post',
    data: data
  })
}

/**
 * 获取机器人模式配置
 * @param {number} robotId - 机器人ID
 * @param {number} modeId - 模式ID
 * @returns {Promise}
 */
export function getRobotModeConfig(robotId, modeId) {
  return request({
    url: '/system/robot/modeConfig',
    method: 'get',
    params: { robotId, modeId }
  })
}

/**
 * 批量保存机器人模式配置
 * @param {Object} data - 批量配置数据
 * @param {number[]} data.robotIds - 机器人ID数组
 * @param {number} data.modeId - 模式ID
 * @param {Object} data.config - 配置参数
 * @returns {Promise}
 */
export function batchSaveRobotModeConfig(data) {
  return request({
    url: '/system/robot/batchSaveModeConfig',
    method: 'post',
    data: data
  })
}

/**
 * 删除机器人模式配置
 * @param {number} robotId - 机器人ID
 * @param {number} modeId - 模式ID
 * @returns {Promise}
 */
export function deleteRobotModeConfig(robotId, modeId) {
  return request({
    url: '/system/robot/deleteModeConfig',
    method: 'delete',
    params: { robotId, modeId }
  })
}

/**
 * 复制机器人模式配置
 * @param {Object} data - 复制配置数据
 * @param {number} data.sourceRobotId - 源机器人ID
 * @param {number} data.targetRobotId - 目标机器人ID
 * @param {number} data.modeId - 模式ID
 * @returns {Promise}
 */
export function copyRobotModeConfig(data) {
  return request({
    url: '/system/robot/copyModeConfig',
    method: 'post',
    data: data
  })
}
