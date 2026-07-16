import request from '@/utils/request'

// 获取导航配置（增加 robotId 参数）
export function getNavConfig(robotId) {
  return request({
    url: '/func/nav/config',
    method: 'get',
    params: { robotId: robotId }
  })
}

// 保存导航配置（data 中需包含 robotId）
export function saveNavConfig(data) {
  return request({
    url: '/func/nav/config',
    method: 'post',
    data: data
  })
}

