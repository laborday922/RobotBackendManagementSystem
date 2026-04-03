import request from '@/utils/request'

// 生成报告
export function generateReport(data) {
  return request({
    url: '/ai/reports/generate',
    method: 'post',
    data,
    timeout: 60000,
    loading: true
  })
}

// 获取报告列表
export function getReportList(params) {
  return request({
    url: '/ai/reports',
    method: 'get',
    params
  })
}

// 下载报告
export function downloadReport(id, format) {
  return request({
    url: `/ai/reports/${id}/download`,
    method: 'get',
    params: { format },
    responseType: 'blob'
  })
}

// 获取报告详情
export function getReportDetail(id) {
  return request({
    url: `/ai/reports/${id}`,
    method: 'get'
  })
}

// 删除报告
export function deleteReport(id) {
  return request({
    url: `/ai/reports/${id}`,
    method: 'delete'
  })
}

// 获取问题分类分布
export function getIssueDistribution(params) {
  return request({
    url: '/ai/issue-classification/distribution',
    method: 'get',
    params
  })
}
