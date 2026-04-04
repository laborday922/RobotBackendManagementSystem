import request from '@/utils/request'

export function getFeedbackWordCloud(params) {
  return request({
    url: '/dashboard/feedback/wordcloud',
    method: 'get',
    params,
    timeout:60000
  })
}

export function getRobotGeo() {
  return request({
    url: '/dashboard/service/robots/geo',
    method: 'get'
  })
}

export function getServiceOverview() {
  return request({
    url: '/dashboard/service/overview',
    method: 'get'
  })
}

export function getTaskExecutions(params) {
  return request({
    url: '/dashboard/tasks/executions',
    method: 'get',
    params
  })
}

export function getRobotGroups() {
  return request({
    url: '/dashboard/robot/groups',
    method: 'get'
  })
}

export function getAnomalyTrend(params) {
  return request({
    url: '/dashboard/anomaly/trend',
    method: 'get',
    params
  })
}
