import request from '@/utils/request'

// 查询模式列表
export function listMode(query) {
  return request({
    url: '/system/mode/list',
    method: 'get',
    params: query
  })
}

// 查询模式详细
export function getMode(modeId) {
  return request({
    url: '/system/mode/' + modeId,
    method: 'get'
  })
}

// 新增模式
export function addMode(data) {
  return request({
    url: '/system/mode',
    method: 'post',
    data: data
  })
}

// 修改模式
export function updateMode(data) {
  return request({
    url: '/system/mode',
    method: 'put',
    data: data
  })
}

// 删除模式
export function delMode(modeId) {
  return request({
    url: '/system/mode/' + modeId,
    method: 'delete'
  })
}

// 修改模式状态
export function changeModeStatus(data) {
  return request({
    url: '/system/mode/changeStatus',
    method: 'put',
    data: data
  })
}

// ==================== 充电策略参数预设 ====================

// 获取充电策略参数模板
export function getChargeStrategyParam() {
  return {
    paramId: null,
    paramName: '充电策略',
    paramType: 'select',
    paramDescription: '选择机器人充电方式：立即充电或完成任务后充电',
    paramValue: 'after_task',
    paramOptions: [
      { label: '立即充电', value: 'immediate' },
      { label: '完成任务后充电', value: 'after_task' }
    ],
    paramMin: null,
    paramMax: null,
    paramUnit: null
  }
}
