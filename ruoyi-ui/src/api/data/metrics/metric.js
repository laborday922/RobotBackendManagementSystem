import request from '@/utils/request'

// 获取所有指标列表
export function listMetrics() {
  return request({
    url: '/metric/list',
    method: 'get'
  })
}

// 根据id获取单个指标
export function getMetricById(id) {
  return request({
    url: `/metric/${id}`,
    method: 'get'
  })
}

// 创建新指标
export function createMetric(data) {
  return request({
    url: '/metric/create',
    method: 'post',
    data
  })
}

// 更新指标
export function updateMetric(data) {
  return request({
    url: '/metric/update',
    method: 'put',
    data
  })
}

// 删除指标
export function deleteMetric(id) {
  return request({
    url: `/metric/${id}`,
    method: 'delete'
  })
}

//获取数据库字段
export function getTableFields(tableName) {
  return request({
    url: '/metric/fields',
    method: 'get',
    params: { tableName }
  })
}

// 计算指标
export function computeMetric(metricId) {
  return request({
    url: `/metric/compute/${metricId}`,
    method: 'post'
  })
}

// 获取图表数据
export function getChartData(metricId) {
  return request({
    url: `/metric/chart/${metricId}`,
    method: 'get'
  })
}
