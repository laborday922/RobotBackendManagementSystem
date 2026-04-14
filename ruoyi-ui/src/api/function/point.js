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
