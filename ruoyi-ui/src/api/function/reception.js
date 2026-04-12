import request from '@/utils/request'

// 获取业务接待配置
export function getReceptionConfig(robotId) {
  return request({
    url: '/func/reception/config/' + robotId,
    method: 'get'
  })
}

// 保存业务接待配置
export function saveReceptionConfig(data) {
  return request({
    url: '/func/reception/config',
    method: 'post',
    data: data
  })
}

// 重置为默认配置
export function resetReceptionConfig(robotId) {
  return request({
    url: '/func/reception/reset/' + robotId,
    method: 'put'
  })
}
