import request from '@/utils/request'

// 获取导航配置
export function getNavConfig() {
  return request({
    url: '/func/nav/config',
    method: 'get'
  })
}

// 保存导航配置
export function saveNavConfig(data) {
  return request({
    url: '/func/nav/config',
    method: 'post',
    data: data
  })
}

// 上传地图
export function uploadNavMap(data) {
  return request({
    url: '/func/nav/map',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 开始导航
export function startNavigation(pointName) {
  return request({
    url: '/func/nav/start/' + pointName,
    method: 'post'
  })
}

// 紧急停止
export function emergencyStop() {
  return request({
    url: '/func/nav/stop',
    method: 'post'
  })
}
